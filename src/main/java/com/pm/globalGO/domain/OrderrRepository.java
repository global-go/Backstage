package com.pm.globalGO.domain;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface OrderrRepository extends JpaRepository<Orderr,Long>{

	List<Orderr> findByUserid(String userID);

	@Transactional
	void deleteByUserid(String userID);


	
}
