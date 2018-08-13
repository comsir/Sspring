package song.sspring.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.security.auth.login.Configuration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class sDispatcherServlet extends HttpServlet{
	private Properties p = new Properties();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		dodispatcher(req,resp);
	}
	
	private void dodispatcher(HttpServletRequest req, HttpServletResponse resp) {

		
	}

	@Override
	public void init(ServletConfig config) throws ServletException {

		System.out.println("1.加载配置文件");
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		
		System.out.println("2.扫描所有类");
		doScanner(p.getProperty("scanPackage"));
		
		System.out.println("3.初始化相关的类，并将其放入IOC容器中");
		doInstence();
		
		System.out.println("4.依赖注入");
		doAutowrited();
		
		System.out.println("5.初始化handlerMapping");
		initHandlerMapping();
		
		System.out.println("=========================55=================");
	
	}

	private void initHandlerMapping() {
		// TODO Auto-generated method stub
		
	}

	private void doAutowrited() {
		// TODO Auto-generated method stub
		
	}

	private void doInstence() {
		// TODO Auto-generated method stub
		
	}

	private void doScanner(String packageName) {

		String url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/")).getFile();
	}

	private void doLoadConfig(String contextConfigLocation) {

		InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
		try {
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(null!=is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
