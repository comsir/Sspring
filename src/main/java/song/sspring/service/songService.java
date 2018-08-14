package song.sspring.service;

import song.sspring.annotation.sService;

@sService
public class songService implements IsongService {

	@Override
	public String getName(String name) {
		return "my name is"+name;
	}

}
