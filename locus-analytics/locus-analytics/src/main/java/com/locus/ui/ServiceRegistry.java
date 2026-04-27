package com.locus.ui;

import com.locus.service.impl.AuthenticationServiceImpl;
import com.locus.dao.impl.UserDAOImpl;
import com.locus.dao.UserDAO;

import com.locus.service.AuthenticationService;
import com.locus.service.CompareService;
import com.locus.service.ConfigurationService;
import com.locus.service.ETLService;
import com.locus.service.HeatmapService;
import com.locus.service.InvestmentClusterService;
import com.locus.service.ListingManagementService;
import com.locus.service.PriceTrendService;
import com.locus.service.ROIService;
import com.locus.service.RentalYieldService;
import com.locus.service.SearchService;
import com.locus.service.ValuationReportService;
import com.locus.service.ValuationService;
import com.locus.service.impl.AuthenticationServiceStub;
import com.locus.service.impl.CompareServiceStub;
import com.locus.service.impl.ConfigurationServiceStub;
import com.locus.service.impl.ETLServiceStub;
import com.locus.service.impl.HeatmapServiceStub;
import com.locus.service.impl.InvestmentClusterServiceStub;
import com.locus.service.impl.ListingManagementServiceStub;
import com.locus.service.impl.PriceTrendServiceStub;
import com.locus.service.impl.ROIServiceStub;
import com.locus.service.impl.RentalYieldServiceStub;
import com.locus.service.impl.SearchServiceStub;
import com.locus.service.impl.ValuationReportServiceStub;
import com.locus.service.impl.ValuationServiceStub;

/**
 * Central registry for service dependencies.
 * Uses stub implementations during Phase 1 scaffolding.
 */
public class ServiceRegistry {

    private final AuthenticationService authenticationService = new AuthenticationServiceImpl(new UserDAOImpl());
    private final ValuationService valuationService = new ValuationServiceStub();
    private final RentalYieldService rentalYieldService = new RentalYieldServiceStub();
    private final ROIService roiService = new ROIServiceStub();
    private final SearchService searchService = new SearchServiceStub();
    private final CompareService compareService = new CompareServiceStub();
    private final PriceTrendService priceTrendService = new PriceTrendServiceStub();
    private final HeatmapService heatmapService = new HeatmapServiceStub();
    private final InvestmentClusterService investmentClusterService = new InvestmentClusterServiceStub();
    private final ValuationReportService valuationReportService = new ValuationReportServiceStub();
    private final ETLService etlService = new ETLServiceStub();
    private final ListingManagementService listingManagementService = new ListingManagementServiceStub();
    private final ConfigurationService configurationService = new ConfigurationServiceStub();

    public AuthenticationService authenticationService() {
        return authenticationService;
    }

    public ValuationService valuationService() {
        return valuationService;
    }

    public RentalYieldService rentalYieldService() {
        return rentalYieldService;
    }

    public ROIService roiService() {
        return roiService;
    }

    public SearchService searchService() {
        return searchService;
    }

    public CompareService compareService() {
        return compareService;
    }

    public PriceTrendService priceTrendService() {
        return priceTrendService;
    }

    public HeatmapService heatmapService() {
        return heatmapService;
    }

    public InvestmentClusterService investmentClusterService() {
        return investmentClusterService;
    }

    public ValuationReportService valuationReportService() {
        return valuationReportService;
    }

    public ETLService etlService() {
        return etlService;
    }

    public ListingManagementService listingManagementService() {
        return listingManagementService;
    }

    public ConfigurationService configurationService() {
        return configurationService;
    }
}
