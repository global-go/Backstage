package com.pm.globalGO.domain;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,CartPK>{

	Cart findByUserIDAndCommodityID(String userID,String commodityID);
	CartPK deleteByUserIDAndCommodityID(String userID,String commodityID);
	List<Cart> findByUserID(String userID);
	void deleteByUserID(String userID);
}
