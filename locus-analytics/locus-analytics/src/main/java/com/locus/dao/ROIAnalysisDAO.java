package com.locus.dao;
import com.locus.model.ROIAnalysis;

public interface ROIAnalysisDAO {

    ROIAnalysis findByProperty(String propertyId);

    boolean insert(ROIAnalysis analysis);
}
