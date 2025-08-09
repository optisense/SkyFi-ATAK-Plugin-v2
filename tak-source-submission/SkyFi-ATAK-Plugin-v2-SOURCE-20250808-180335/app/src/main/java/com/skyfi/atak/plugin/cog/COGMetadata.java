package com.skyfi.atak.plugin.cog;

import com.atakmap.coremap.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses Cloud Optimized GeoTIFF metadata from HTTP headers
 * Reads TIFF IFDs (Image File Directories) to understand the COG structure
 */
public class COGMetadata {
    
    private static final String TAG = "SkyFi.COGMetadata";
    
    // TIFF constants
    private static final int TIFF_LITTLE_ENDIAN = 0x4949;
    private static final int TIFF_BIG_ENDIAN = 0x4D4D;
    private static final int TIFF_MAGIC = 42;
    
    // GeoTIFF tags we care about
    private static final int TAG_IMAGE_WIDTH = 256;
    private static final int TAG_IMAGE_HEIGHT = 257;
    private static final int TAG_TILE_WIDTH = 322;
    private static final int TAG_TILE_HEIGHT = 323;
    private static final int TAG_TILE_OFFSETS = 324;
    private static final int TAG_TILE_BYTE_COUNTS = 325;
    private static final int TAG_COMPRESSION = 259;
    private static final int TAG_PHOTOMETRIC = 262;
    private static final int TAG_SAMPLES_PER_PIXEL = 277;
    private static final int TAG_BITS_PER_SAMPLE = 258;
    private static final int TAG_MODEL_PIXEL_SCALE = 33550;
    private static final int TAG_MODEL_TIEPOINT = 33922;
    
    private String cogUrl;
    private boolean isBigEndian;
    private List<IFD> overviews;
    private double[] geoTransform;
    private int epsgCode = 4326; // Default to WGS84
    
    /**
     * Image File Directory - represents one overview level
     */
    public static class IFD {
        public int width;
        public int height;
        public int tileWidth;
        public int tileHeight;
        public long[] tileOffsets;
        public long[] tileByteCounts;
        public int compression;
        public int photometric;
        public int samplesPerPixel;
        public int bitsPerSample;
        public double pixelScale;
        
        public int getTilesAcross() {
            return (width + tileWidth - 1) / tileWidth;
        }
        
        public int getTilesDown() {
            return (height + tileHeight - 1) / tileHeight;
        }
        
        public int getTileIndex(int tileX, int tileY) {
            return tileY * getTilesAcross() + tileX;
        }
    }
    
    private COGMetadata() {
        this.overviews = new ArrayList<>();
        this.geoTransform = new double[6];
    }
    
    /**
     * Read COG metadata from URL using HTTP range requests
     */
    public static COGMetadata readFromUrl(String cogUrl) throws IOException {
        COGMetadata metadata = new COGMetadata();
        metadata.cogUrl = cogUrl;
        
        // Read first 16KB to get header and first IFD
        byte[] headerData = fetchBytes(cogUrl, 0, 16384);
        ByteBuffer buffer = ByteBuffer.wrap(headerData);
        
        // Read TIFF header
        metadata.readHeader(buffer);
        
        // Read IFDs (overview levels)
        metadata.readIFDs(buffer);
        
        Log.d(TAG, "Read COG metadata: " + metadata.overviews.size() + " overview levels");
        return metadata;
    }
    
    private void readHeader(ByteBuffer buffer) throws IOException {
        // Check byte order
        short byteOrder = buffer.getShort();
        if (byteOrder == TIFF_LITTLE_ENDIAN) {
            isBigEndian = false;
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        } else if (byteOrder == TIFF_BIG_ENDIAN) {
            isBigEndian = true;
            buffer.order(ByteOrder.BIG_ENDIAN);
        } else {
            throw new IOException("Invalid TIFF byte order marker: " + byteOrder);
        }
        
        // Check magic number
        short magic = buffer.getShort();
        if (magic != TIFF_MAGIC) {
            throw new IOException("Invalid TIFF magic number: " + magic);
        }
        
        Log.d(TAG, "TIFF header valid, byte order: " + (isBigEndian ? "big" : "little"));
    }
    
    private void readIFDs(ByteBuffer buffer) throws IOException {
        // Get offset to first IFD
        int ifdOffset = buffer.getInt();
        
        while (ifdOffset != 0 && overviews.size() < 10) { // Limit to 10 overviews
            buffer.position(ifdOffset);
            
            IFD ifd = new IFD();
            int numEntries = buffer.getShort() & 0xFFFF;
            
            for (int i = 0; i < numEntries; i++) {
                int tag = buffer.getShort() & 0xFFFF;
                int type = buffer.getShort() & 0xFFFF;
                int count = buffer.getInt();
                int valueOffset = buffer.getInt();
                
                // Save current position
                int savedPos = buffer.position();
                
                switch (tag) {
                    case TAG_IMAGE_WIDTH:
                        ifd.width = readIntValue(buffer, type, valueOffset);
                        break;
                    case TAG_IMAGE_HEIGHT:
                        ifd.height = readIntValue(buffer, type, valueOffset);
                        break;
                    case TAG_TILE_WIDTH:
                        ifd.tileWidth = readIntValue(buffer, type, valueOffset);
                        break;
                    case TAG_TILE_HEIGHT:
                        ifd.tileHeight = readIntValue(buffer, type, valueOffset);
                        break;
                    case TAG_COMPRESSION:
                        ifd.compression = readIntValue(buffer, type, valueOffset);
                        break;
                    case TAG_PHOTOMETRIC:
                        ifd.photometric = readIntValue(buffer, type, valueOffset);
                        break;
                    case TAG_SAMPLES_PER_PIXEL:
                        ifd.samplesPerPixel = readIntValue(buffer, type, valueOffset);
                        break;
                    case TAG_BITS_PER_SAMPLE:
                        ifd.bitsPerSample = readIntValue(buffer, type, valueOffset);
                        break;
                    case TAG_TILE_OFFSETS:
                        ifd.tileOffsets = readLongArray(buffer, type, count, valueOffset);
                        break;
                    case TAG_TILE_BYTE_COUNTS:
                        ifd.tileByteCounts = readLongArray(buffer, type, count, valueOffset);
                        break;
                    case TAG_MODEL_PIXEL_SCALE:
                        readPixelScale(buffer, ifd, valueOffset);
                        break;
                }
                
                // Restore position
                buffer.position(savedPos);
            }
            
            overviews.add(ifd);
            
            // Get offset to next IFD
            ifdOffset = buffer.getInt();
            
            Log.d(TAG, String.format("Read IFD %d: %dx%d, tiles %dx%d", 
                overviews.size(), ifd.width, ifd.height, ifd.tileWidth, ifd.tileHeight));
        }
    }
    
    private int readIntValue(ByteBuffer buffer, int type, int offset) {
        if (type == 3) { // SHORT
            return offset & 0xFFFF; // Value is stored in offset field
        } else if (type == 4) { // LONG
            return offset; // Value is stored in offset field
        }
        return 0;
    }
    
    private long[] readLongArray(ByteBuffer buffer, int type, int count, int offset) {
        long[] array = new long[count];
        
        // For large arrays, offset points to the data
        if (count * 4 > 4) {
            buffer.position(offset);
        }
        
        for (int i = 0; i < count; i++) {
            if (type == 4) { // LONG
                array[i] = buffer.getInt() & 0xFFFFFFFFL;
            } else if (type == 3) { // SHORT
                array[i] = buffer.getShort() & 0xFFFF;
            }
        }
        
        return array;
    }
    
    private void readPixelScale(ByteBuffer buffer, IFD ifd, int offset) {
        buffer.position(offset);
        double xScale = buffer.getDouble();
        double yScale = buffer.getDouble();
        ifd.pixelScale = Math.abs(xScale); // Use absolute value
    }
    
    /**
     * Get the appropriate overview level for a given zoom
     */
    public int getOverviewForZoom(int zoom) {
        // Simple strategy: use full resolution for high zooms,
        // progressively lower resolution for lower zooms
        if (zoom >= 18) return 0; // Full resolution
        if (zoom >= 16) return Math.min(1, overviews.size() - 1);
        if (zoom >= 14) return Math.min(2, overviews.size() - 1);
        if (zoom >= 12) return Math.min(3, overviews.size() - 1);
        return Math.min(4, overviews.size() - 1);
    }
    
    /**
     * Calculate byte range for a specific tile
     */
    public long[] getTileByteRange(int overviewLevel, int tileX, int tileY) {
        if (overviewLevel >= overviews.size()) {
            return null;
        }
        
        IFD ifd = overviews.get(overviewLevel);
        
        // Check bounds
        if (tileX >= ifd.getTilesAcross() || tileY >= ifd.getTilesDown()) {
            return null;
        }
        
        int tileIndex = ifd.getTileIndex(tileX, tileY);
        if (tileIndex >= ifd.tileOffsets.length) {
            return null;
        }
        
        long offset = ifd.tileOffsets[tileIndex];
        long byteCount = ifd.tileByteCounts[tileIndex];
        
        return new long[] { offset, offset + byteCount - 1 };
    }
    
    /**
     * Fetch bytes from URL using HTTP range request
     */
    private static byte[] fetchBytes(String url, long start, long length) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("Range", String.format("bytes=%d-%d", start, start + length - 1));
        connection.setRequestMethod("GET");
        
        try {
            try (InputStream is = connection.getInputStream()) {
                byte[] buffer = new byte[(int) length];
                int totalRead = 0;
                while (totalRead < length) {
                    int read = is.read(buffer, totalRead, (int) (length - totalRead));
                    if (read == -1) break;
                    totalRead += read;
                }
                return buffer;
            }
        } finally {
            connection.disconnect();
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("COGMetadata{");
        sb.append("overviews=").append(overviews.size());
        if (!overviews.isEmpty()) {
            IFD first = overviews.get(0);
            sb.append(", fullRes=").append(first.width).append("x").append(first.height);
            sb.append(", tileSize=").append(first.tileWidth).append("x").append(first.tileHeight);
        }
        sb.append("}");
        return sb.toString();
    }
}