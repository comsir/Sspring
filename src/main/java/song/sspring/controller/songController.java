package song.sspring.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import song.sspring.annotation.sAutowrited;
import song.sspring.annotation.sController;
import song.sspring.annotation.sRequestParam;
import song.sspring.annotation.sReuestMapping;
import song.sspring.service.IsService;

@sController
@sReuestMapping("/song")
public class songController {

	@sAutowrited
	private IsService sservice;
	@sReuestMapping("/query.json")
	public void query(HttpServletRequest request,HttpServletResponse response,@sRequestParam("name") String name) throws Exception {
		String result = sservice.getName(name);
		response.getWriter().write(result);
	}
	
	@sReuestMapping("/add.json")
	public void add(HttpServletRequest request,HttpServletResponse response,@sRequestParam("a") String a,@sRequestParam("b") String b) throws Exception {
		response.getWriter().write(a+"+"+b+"="+(a+b));
	}

	@sReuestMapping("/edit.json")
	public void edit(HttpServletRequest request,HttpServletResponse response,@sRequestParam("name") String name) throws Exception {
		String result = sservice.getName(name);
		response.getWriter().write(result);
	}
}
