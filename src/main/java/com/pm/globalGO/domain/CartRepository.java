package com.pm.globalGO.domain;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,CartPK>{

	List<Cart> findByUserid(String userID);
	void deleteByUserid(String userID);
}
