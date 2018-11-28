package com.pm.globalGO.domain;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Order_CommodityRepository extends JpaRepository<Order_Commodity,Long>{

	public List<Order_Commodity> findAll();

	public List<Order_Commodity> findByOrderID(Long orderID);
}
