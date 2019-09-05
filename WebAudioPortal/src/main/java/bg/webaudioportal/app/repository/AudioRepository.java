package bg.webaudioportal.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bg.webaudioportal.app.model.Audio;

@Repository("audioRepository")
public interface AudioRepository extends JpaRepository<Audio, Long> {
	List<Audio> findAudioByUploader(String uploader);
	Audio findById(int id);
}