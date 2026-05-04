package com.locus.dao;
import com.locus.model.RentalAnalysis;
import java.util.List;

public interface RentalAnalysisDAO {

    RentalAnalysis findLatestByProperty(String propertyId);

    List<RentalAnalysis> findByProperty(String propertyId);

    boolean insert(RentalAnalysis analysis);

    double getCityAverageYield(String city);

    double getLocalityAverageYield(String city, String locality);
}
