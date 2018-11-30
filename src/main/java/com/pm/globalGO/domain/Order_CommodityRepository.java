package com.pm.globalGO.domain;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface Order_CommodityRepository extends JpaRepository<Order_Commodity,Long>{

	public List<Order_Commodity> findAll();

	public List<Order_Commodity> findByOrderID(Long orderID);

	@Transactional
	public void deleteByOrderID(Long orderID);
}
