package song.sspring.service;

public class sService implements IsService {

	@Override
	public String getName(String name) {
		return "my name is"+name;
	}

}
