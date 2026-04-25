package com.locus;

import com.locus.model.Property;
import com.locus.model.dto.TimeRange;
import com.locus.model.dto.TrendPoint;
import com.locus.service.impl.AuthenticationServiceStub;
import com.locus.service.impl.PriceTrendServiceStub;
import com.locus.service.impl.ValuationServiceStub;

import java.util.List;

public class TestServices {

    public static void main(String[] args) {
        System.out.println("=== Testing Authentication Service ===");
        AuthenticationServiceStub auth = new AuthenticationServiceStub();
        System.out.println("Logged in as: " + auth.login("analyst@locus.com", "password").getName());

        System.out.println("\n=== Testing Valuation Service ===");
        ValuationServiceStub valService = new ValuationServiceStub();
        Property dummyProp = new Property();
        dummyProp.setPropertyId("test-123");
        System.out.println("Estimated FMV: " + valService.estimateFMV(dummyProp).getEstimatedFmv());
        System.out.println("Comparables Found: " + valService.findComparables(dummyProp).size());

        System.out.println("\n=== Testing Price Trend Service ===");
        PriceTrendServiceStub trendService = new PriceTrendServiceStub();
        List<TrendPoint> trends = trendService.getTrend("Karachi", "DHA Phase 6", "house", TimeRange.ONE_YEAR);
        System.out.println("Generated Trend Points: " + trends.size());
        System.out.println("Latest Point: " + trends.get(trends.size() - 1));
        
        System.out.println("\n✅ All tested services compiled and ran successfully!");
    }
}
