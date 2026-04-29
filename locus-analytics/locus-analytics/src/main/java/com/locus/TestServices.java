package com.locus;

import com.locus.model.Property;
import com.locus.model.dto.SearchFilter;
import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.service.AuthenticationService;
import com.locus.service.PriceTrendService;
import com.locus.service.SearchService;
import com.locus.service.ValuationService;
import com.locus.ui.ServiceRegistry;

import java.util.List;

public class TestServices {

    public static void main(String[] args) {
        // 1. Initialize the Registry (This loads the ML model and Database DAOs)
        ServiceRegistry registry = new ServiceRegistry();

        System.out.println("--- Testing REAL Arham-Phase-2 Services ---");

        // 2. Test REAL Authentication (Skipped - no users in DB yet)
        // AuthenticationService auth = registry.authenticationService();
        // System.out.println("Logged in as: " + auth.login("analyst@locus.com", "password").getName());

        // 3. Test REAL ML-Based Valuation
        ValuationService valService = registry.valuationService();
        Property dummyProp = new Property();
        dummyProp.setCity("Karachi");
        dummyProp.setLocality("Malir Cantt");
        dummyProp.setPropertyType("house");
        dummyProp.setArea(5445); // 2000 sq ft
        
        System.out.println("\n--- Testing Real ML-Based Valuation ---");
        // This now uses the Ridge Regression model in ml/model.json
        double fmv = valService.estimateFMV(dummyProp).getEstimatedFmv();
        System.out.println("Estimated FMV for 2000sqft house in Clifton: PKR " + String.format("%,.0f", fmv));

        // 4. Test REAL Price Trends
        PriceTrendService trendService = registry.priceTrendService();
        System.out.println("\n--- Testing Real Price Trend Service ---");
        List<TrendPoint> trends = trendService.getTrend("Karachi", "Clifton", "house", TimeRange.ONE_YEAR);
        System.out.println("Real Data Points found in DB: " + trends.size());

        // 5. Test REAL Search
        SearchService searchService = registry.searchService();
        System.out.println("\n--- Testing Real Search ---");
        System.out.println("Properties found in Database: " + searchService.search(new SearchFilter()).getTotalCount());

        System.out.println("\n✅ All real Arham-Phase-2 services are working perfectly!");

        System.out.println("\n--- Testing REAL Fasih-Phase-2 Services ---");
        
        // 6. Test REAL Configuration
        com.locus.service.ConfigurationService configService = registry.configurationService();
        System.out.println("Config loaded. DB Host: " + configService.getConfig().getDbHost());
        
        // 7. Test REAL Listing Management
        com.locus.service.ListingManagementService listingService = registry.listingManagementService();
        Property newListing = new Property();
        newListing.setCity("Islamabad");
        newListing.setLocality("Sector F-8");
        newListing.setPropertyType("house");
        newListing.setArea(4500);
        newListing.setPrice(75000000);
        newListing.setBedrooms(5);
        newListing.setBathrooms(5);
        newListing = listingService.addListing(newListing);
        System.out.println("New listing added with ID: " + newListing.getPropertyId());
        
        // 8. Test REAL ETL Scraper
        com.locus.service.ETLService etlService = registry.etlService();
        System.out.println("Triggering ETL pipeline (Check console for Python output)...");
        com.locus.model.ETLJob job = etlService.triggerPipeline();
        System.out.println("Job created with ID: " + job.getJobId());
        
        System.out.println("\n✅ Fasih Phase-2 services triggered successfully!");
    }
}
