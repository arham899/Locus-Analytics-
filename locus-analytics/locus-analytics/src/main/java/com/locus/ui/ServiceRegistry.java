package com.locus.ui;

import com.locus.dao.impl.*;
import com.locus.ml.LinearRegressionPredictor;

import com.locus.service.AuthenticationService;
import com.locus.service.CompareService;
import com.locus.service.ConfigurationService;
import com.locus.service.ETLService;
import com.locus.service.HeatmapService;
import com.locus.service.InvestmentClusterService;
import com.locus.service.ListingManagementService;
import com.locus.service.PriceTrendService;
import com.locus.service.ReportPdfService;
import com.locus.service.ROIService;
import com.locus.service.RentalYieldService;
import com.locus.service.SearchService;
import com.locus.service.ValuationReportService;
import com.locus.service.ValuationService;

// Real implementations (Phase 2 — Arham)
import com.locus.service.impl.AuthenticationServiceImpl;
import com.locus.service.impl.ValuationServiceImpl;
import com.locus.service.impl.RentalYieldServiceImpl;
import com.locus.service.impl.ROIServiceImpl;
import com.locus.service.impl.SearchServiceImpl;
import com.locus.service.impl.CompareServiceImpl;
import com.locus.service.impl.PriceTrendServiceImpl;
import com.locus.service.impl.ReportPdfServiceImpl;
import com.locus.service.impl.HeatmapServiceImpl;
import com.locus.service.impl.InvestmentClusterServiceImpl;

// Real implementations (Phase 2 — Fasih)
import com.locus.service.impl.ValuationReportServiceImpl;
import com.locus.service.impl.ETLServiceImpl;
import com.locus.service.impl.ListingManagementServiceImpl;
import com.locus.service.impl.ConfigurationServiceImpl;

/**
 * Central registry for service dependencies.
 *
 * <p>Phase 2 complete: All 12 core business services are now fully
 * implemented and wired to their respective DAOs.</p>
 */
public class ServiceRegistry {

// ── Shared DAO instances ────────────────────────
    private final PropertyDAOImpl propertyDAO = new PropertyDAOImpl();
    private final ValuationDAOImpl valuationDAO = new ValuationDAOImpl();
    private final RentalAnalysisDAOImpl rentalAnalysisDAO = new RentalAnalysisDAOImpl();
    private final ROIAnalysisDAOImpl roiAnalysisDAO = new ROIAnalysisDAOImpl();
    private final InvestmentClusterDAOImpl clusterDAO = new InvestmentClusterDAOImpl();
    private final UserDAOImpl userDAO = new UserDAOImpl();
    private final SystemConfigurationDAOImpl configDAO = new SystemConfigurationDAOImpl();
    private final ETLJobDAOImpl etlJobDAO = new ETLJobDAOImpl();

    // ── ML Predictor ────────────────────────────────
    private final LinearRegressionPredictor predictor = new LinearRegressionPredictor("ml/model.json");

    // ── Real Services (Phase 2 — Arham) ─────────────
    private final AuthenticationService authenticationService = new AuthenticationServiceImpl(userDAO);
    private final ValuationService valuationService = new ValuationServiceImpl(propertyDAO, valuationDAO, predictor);
    private final RentalYieldService rentalYieldService = new RentalYieldServiceImpl(rentalAnalysisDAO);
    private final ROIService roiService = new ROIServiceImpl(roiAnalysisDAO);
    private final SearchService searchService = new SearchServiceImpl(propertyDAO);
    private final CompareService compareService = new CompareServiceImpl(propertyDAO, valuationDAO);
    private final PriceTrendService priceTrendService = new PriceTrendServiceImpl(propertyDAO);
    private final HeatmapService heatmapService = new HeatmapServiceImpl(propertyDAO);
    private final InvestmentClusterService investmentClusterService = new InvestmentClusterServiceImpl(propertyDAO, clusterDAO);

    // ── Real Services (Phase 2 — Fasih) ─────────────
    private final ValuationReportService valuationReportService = new ValuationReportServiceImpl(propertyDAO, valuationDAO);
    private final ReportPdfService reportPdfService = new ReportPdfServiceImpl();
    private final ETLService etlService = new ETLServiceImpl(etlJobDAO);
    private final ListingManagementService listingManagementService = new ListingManagementServiceImpl(propertyDAO);
    private final ConfigurationService configurationService = new ConfigurationServiceImpl(configDAO);

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

    public ReportPdfService reportPdfService() {
        return reportPdfService;
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
