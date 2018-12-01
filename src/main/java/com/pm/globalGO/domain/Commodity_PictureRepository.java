package com.pm.globalGO.domain;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Commodity_PictureRepository extends JpaRepository<Commodity_Picture,Commodity_PicturePK>{

	public List<Commodity_Picture> findAll();

	public List<Commodity_Picture> findByCommodityid(Long commodityID);
}
