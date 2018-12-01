package com.pm.globalGO.domain;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommodityRepository extends JpaRepository<Commodity,Long>{

	Commodity findByCommodityid(Long commodityID);
	List<Commodity> findAll();
	
}
