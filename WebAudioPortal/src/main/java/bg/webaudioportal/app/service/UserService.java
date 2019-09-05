package bg.webaudioportal.app.service;
import java.util.List;

import bg.webaudioportal.app.model.Audio;
import bg.webaudioportal.app.model.User;

public interface UserService {
	public User findUserByEmail(String email);
	public void saveUser(User user);
	public List<Audio> findAudioByUploader(String uploader);
	public void saveAudio(Audio audio);
	public Audio findAudioById(int id);
	public void deleteAudio(Audio audio);
}