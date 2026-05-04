import hashlib

def clean_price(price_str):
    """
    Handles strings like '12,000,000 PKR', ' 10000 ', or even raw integers.
    """
    if isinstance(price_str, (int, float)):
        return int(price_str)
    
    numeric_string = "".join(filter(str.isdigit, str(price_str)))
    
    return int(numeric_string) if numeric_string else 0

def clean_text(text):
    return str(text).strip() if text else ""

def clean_property_type(ptype):
    if not ptype:
        return "Unknown"
    return ptype.strip().capitalize()

def clean_area(area_str):
    """
    Standardizes area to Marla.
    Supports: '5 Marla', '1 Kanal', '500 Sq. Ft.'
    """
    if not area_str:
        return 10.0 # Default fallback
    
    area_str = str(area_str).lower().strip()
    
    # Extract numeric part
    import re
    match = re.search(r"(\d+\.?\d*)", area_str)
    if not match:
        return 10.0
    
    value = float(match.group(1))
    
    if "kanal" in area_str:
        return value * 20.0
    elif "sq. ft." in area_str or "sqft" in area_str:
        return value / 225.0
    else:
        # Assume Marla if no unit or unit is Marla
        return value

def generate_id(title, price):
    raw_string = f"{title}{price}"
    return "P_" + hashlib.md5(raw_string.encode()).hexdigest()[:10]

if __name__ == "__main__":
    # Test cases
    print(f"Price: {clean_price(' 12,500,000 PKR ')}") # Expected: 12500000
    print(f"Text: '{clean_text('  DHA Phase 6  ')}'")  # Expected: 'DHA Phase 6'
    print(f"Type: {clean_property_type('house')}")      # Expected: House
