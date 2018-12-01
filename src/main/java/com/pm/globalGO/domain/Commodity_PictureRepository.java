package com.pm.globalGO.domain;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface Commodity_PictureRepository extends JpaRepository<Commodity_Picture,Commodity_PicturePK>{

	public List<Commodity_Picture> findAll();

	public List<Commodity_Picture> findByCommodityid(Long commodityID);
	
	@Transactional
	public void deleteByCommodityid(Long commodityid);
}
