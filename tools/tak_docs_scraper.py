import asyncio
from playwright.async_api import async_playwright
import os
from datetime import datetime
import re

async def capture_tak_documentation():
    screenshot_dir = "tak_documentation_screenshots"
    os.makedirs(screenshot_dir, exist_ok=True)
    
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=False)
        context = await browser.new_context(
            viewport={'width': 1920, 'height': 1080},
            device_scale_factor=1.0
        )
        page = await context.new_page()
        
        base_url = "https://tak.gov/documentation/resources/tak-developers/developer-documentation"
        
        print(f"Navigating to {base_url}")
        await page.goto(base_url, wait_until="networkidle")
        
        # Wait for user to log in
        print("\n" + "="*60)
        print("PLEASE LOG IN TO THE WEBSITE")
        print("You have 45 seconds to authenticate")
        print("The script will continue automatically after the wait period")
        print("="*60 + "\n")
        
        # Wait for user to log in
        for i in range(45, 0, -5):
            print(f"Waiting... {i} seconds remaining")
            await page.wait_for_timeout(5000)
        
        print("Continuing with screenshot capture...")
        await page.wait_for_timeout(2000)
        
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        
        screenshot_path = os.path.join(screenshot_dir, f"tak_main_page_{timestamp}.png")
        await page.screenshot(path=screenshot_path, full_page=True)
        print(f"Screenshot saved: {screenshot_path}")
        
        visited_links = set()
        visited_links.add(base_url)
        
        async def process_page_links(current_url):
            try:
                await page.goto(current_url, wait_until="networkidle", timeout=30000)
                await page.wait_for_timeout(2000)
                
                links = await page.locator('a[href*="/documentation/"]').all()
                
                for link in links:
                    try:
                        href = await link.get_attribute('href')
                        if href:
                            if href.startswith('/'):
                                full_url = f"https://tak.gov{href}"
                            elif href.startswith('http'):
                                full_url = href
                            else:
                                continue
                            
                            if (full_url not in visited_links and 
                                "tak.gov/documentation" in full_url and
                                not any(ext in full_url for ext in ['.pdf', '.zip', '.tar', '.gz'])):
                                
                                visited_links.add(full_url)
                                print(f"\nNavigating to: {full_url}")
                                
                                try:
                                    await page.goto(full_url, wait_until="networkidle", timeout=30000)
                                    await page.wait_for_timeout(2000)
                                    
                                    page_title = await page.title()
                                    page_title_clean = re.sub(r'[^\w\s-]', '', page_title).strip()
                                    page_title_clean = re.sub(r'[-\s]+', '-', page_title_clean)[:50]
                                    
                                    screenshot_name = f"{page_title_clean}_{len(visited_links)}.png"
                                    screenshot_path = os.path.join(screenshot_dir, screenshot_name)
                                    
                                    await page.screenshot(path=screenshot_path, full_page=True)
                                    print(f"Screenshot saved: {screenshot_path}")
                                    
                                    await asyncio.sleep(1)
                                    
                                except Exception as e:
                                    print(f"Error processing {full_url}: {str(e)}")
                                    continue
                    
                    except Exception as e:
                        print(f"Error processing link: {str(e)}")
                        continue
                
            except Exception as e:
                print(f"Error on page {current_url}: {str(e)}")
        
        await process_page_links(base_url)
        
        print(f"\n\nProcessing additional documentation sections...")
        documentation_sections = [
            "/documentation/resources/tak-developers",
            "/documentation/resources/tak-administrators",
            "/documentation/resources/tak-users",
            "/documentation/downloads",
            "/documentation/releases"
        ]
        
        for section in documentation_sections:
            section_url = f"https://tak.gov{section}"
            if section_url not in visited_links:
                try:
                    print(f"\nChecking section: {section_url}")
                    await page.goto(section_url, wait_until="networkidle", timeout=30000)
                    await page.wait_for_timeout(2000)
                    
                    page_title = await page.title()
                    page_title_clean = re.sub(r'[^\w\s-]', '', page_title).strip()
                    page_title_clean = re.sub(r'[-\s]+', '-', page_title_clean)[:50]
                    
                    screenshot_name = f"{page_title_clean}_{len(visited_links)}.png"
                    screenshot_path = os.path.join(screenshot_dir, screenshot_name)
                    
                    await page.screenshot(path=screenshot_path, full_page=True)
                    print(f"Screenshot saved: {screenshot_path}")
                    
                    visited_links.add(section_url)
                    await process_page_links(section_url)
                    
                except Exception as e:
                    print(f"Error accessing section {section_url}: {str(e)}")
        
        await browser.close()
        
        print(f"\n\nScraping completed!")
        print(f"Total pages visited: {len(visited_links)}")
        print(f"Screenshots saved in: {os.path.abspath(screenshot_dir)}")

if __name__ == "__main__":
    asyncio.run(capture_tak_documentation())