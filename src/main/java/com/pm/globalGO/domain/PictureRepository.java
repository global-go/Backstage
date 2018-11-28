package com.pm.globalGO.domain;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PictureRepository extends JpaRepository<Picture,Long>{

	public List<Picture> findAll();

	public Picture findByPictureIndex(Long pictureIndex);

	
}
