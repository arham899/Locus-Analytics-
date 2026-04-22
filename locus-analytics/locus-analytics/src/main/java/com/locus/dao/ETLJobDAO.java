package com.locus.dao;
import com.locus.model.ETLJob;
import java.util.List;

public interface ETLJobDAO {

    List<ETLJob> findAll();

    ETLJob findLatest();

    boolean insert(ETLJob job);
}
