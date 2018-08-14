package song.sspring.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import song.sspring.annotation.sAutowrited;
import song.sspring.annotation.sController;
import song.sspring.annotation.sRequestParam;
import song.sspring.annotation.sRequestMapping;
import song.sspring.service.IsongService;

@sController
@sRequestMapping("/song")
public class songController {

	@sAutowrited
	private IsongService sservice;
	@sRequestMapping("/query.json")
	public void query(HttpServletRequest request,HttpServletResponse response,@sRequestParam("name") String name) throws Exception {
		String result = sservice.getName(name);
		response.getWriter().write(result);
	}
	
	@sRequestMapping("/add.json")
	public void add(HttpServletRequest request,HttpServletResponse response,@sRequestParam("a") String a,@sRequestParam("b") String b) throws Exception {
		response.getWriter().write(a+"+"+b+"="+(a+b));
	}

	@sRequestMapping("/edit.json")
	public void edit(HttpServletRequest request,HttpServletResponse response,@sRequestParam("name") String name) throws Exception {
		String result = sservice.getName(name);
		response.getWriter().write(result);
	}
}
