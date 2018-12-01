package com.pm.globalGO.domain;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CartRepository extends JpaRepository<Cart,CartPK>{

	List<Cart> findByUserid(String userID);
	
	@Transactional
	void deleteByUserid(String userID);
	
	Cart findByUseridAndCommodityid(String userID, Long commodityID);
	
	
}
