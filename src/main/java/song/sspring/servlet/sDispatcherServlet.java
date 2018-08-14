package song.sspring.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream.PutField;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.Configuration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.Icon;

import song.sspring.annotation.sAutowrited;
import song.sspring.annotation.sController;
import song.sspring.annotation.sRequestMapping;
import song.sspring.annotation.sRequestParam;
import song.sspring.annotation.sService;

public class sDispatcherServlet extends HttpServlet{
	private Properties p = new Properties();
	
	private List<String> classNames = new ArrayList<>();
	
	//创建IOC容器
	//IOC容器不是一个普通的list 是一个map<String,object>
	private Map<String, Object> ioc = new HashMap<>();
	
	private List<Handler> handlerMapping = new ArrayList<>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		dodispatcher(req,resp);
	}
	
	private void dodispatcher(HttpServletRequest req, HttpServletResponse resp) {
		if(handlerMapping.isEmpty()) {return;}
		//拿到请求的路径
		Handler handler = getHandler(req);
		if(handler==null) {
			try {
				resp.getWriter().write("404 找不到资源");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;
		
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
		if(ioc.isEmpty()) {return;}
		//空 则返回，不空则遍历取出map中的所有实体
		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			//拿到这个类
			Class<?> clazz = entry.getValue().getClass();
			//handlerMapping 找的是controller
			if(!clazz.isAnnotationPresent(sController.class)) {continue;}
			
			String url="";
			//1.获取class的requestMapping值
			if(clazz.isAnnotationPresent(sRequestMapping.class)) {
				//如果被sReuestMapping标注，则取出他的路径
				sRequestMapping requestMapping = clazz.getAnnotation(sRequestMapping.class);
				url=requestMapping.value();
			}
			
			//方法地址拿到 然后去拿所有的方法体
			Method[] methods = clazz.getMethods();
			//2.获取method的requestMapping值
			for (Method method : methods) {
				//方法未被标注就退出
				if(!method.isAnnotationPresent(sRequestMapping.class)) {return;}
				//方法被requestMapping标注则......
				sRequestMapping requestMapping = method.getAnnotation(sRequestMapping.class);
				//requestMapping中"/"斜杠数超过1个时 都替换成1个[正则表达式]  造出来一个匹配模式
				String regex = ("/"+url+"/"+requestMapping.value()).replaceAll("/+","/");
				Pattern pattern = Pattern.compile(regex);
				
				handlerMapping.add(new Handler(pattern, entry.getValue(), method));
				System.out.println("add Mapping"+regex+","+method);
				
			}
		}
		
	}

	private void doAutowrited() {

		if(ioc.isEmpty()) {return;}
		//注入无非就是给属性自动赋值
		for (Map.Entry<String,Object> entry : ioc.entrySet()) {
			//拿到属性
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				//如果没有标注 就不需要DI依赖注入
				if(!field.isAnnotationPresent(sAutowrited.class)) {
					continue;
				}
				//如果标记了
				sAutowrited autowrited = field.getAnnotation(sAutowrited.class);
				String beanName = autowrited.value();
				if("".equals(beanName)) {
					//如果是空就用默认值
					beanName = field.getType().getName();
				}
				//不管是不是私有属性
                //暴力访问
                field.setAccessible(true);
                try {
					field.set(entry.getValue(), ioc.get(beanName));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	
	//IOC容器初始化完成
	private void doInstence() {
		//如果扫描的类为空
		if(classNames.isEmpty()) {return;}
		//不为空则迭代取出
		try {
			for (String className : classNames) {
				Class<?> clazz = Class.forName(className);
				//如果被标注为@sController
				if(clazz.isAnnotationPresent(sController.class)) {
					//但是默认是类名首字母小写
					String beanName = lowerFirst(clazz.getSimpleName());
					//若果被标注，则放入IOC容器中
					ioc.put(className, clazz);
				}
				//如果被标注为@sService
				//三种情况：
				//1.默认是类名字母小写
                //2.自定义类名，比如：@service("aaa")会优先使用自定义的
                //3.接口的类型为key存到IOC容器中 初始化他的实现类
				else if(clazz.isAnnotationPresent(sService.class)){
					//优先考虑自定义 拿到service("XXX")中的值
					sService service = clazz.getAnnotation(sService.class);
					String beanName = service.value();
					//如果为空则取默认值
					if("".equals(beanName)) {
						beanName = lowerFirst(clazz.getSimpleName());
					}
					//名字解决了 就new出class的bean对象
					Object newInstance = clazz.newInstance();
					ioc.put(beanName, newInstance);
					
					//如果是接口  迭代初始化所有实现类
					Class<?>[] interfaces = clazz.getInterfaces();
					for (Class<?> i : interfaces) {
						ioc.put(i.getName(), newInstance);
					}
				}else {
					continue;
				}
				
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		
	
	}

	private String lowerFirst(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0]+=32;
		return String.valueOf(chars);
	}

	private void doScanner(String packageName) {

		String url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/")).getFile();
		File dir = new File(url);
		for (File file : dir.listFiles()) {
			//如果file是文件夹 后面加点
			if(file.isDirectory()) {
				doScanner(packageName+"."+file.getName());
			}else {
				String className = (packageName+"."+file.getName()).replaceAll(".class", "");
				//把扫描到的类名放入数组中，待初始化使用
				classNames.add(className);
			}
		}
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
	
	 private Handler getHandler(HttpServletRequest req) {
	        if (handlerMapping.isEmpty()){return null;}

	        String url = req.getRequestURI();
	        String contextPath = req.getContextPath();
	        url = url.replace(contextPath, "").replaceAll("/+","/");

	        for (Handler handler : handlerMapping) {
	            Matcher matcher = handler.pattern.matcher(url);
	            if (!matcher.matches()){continue;}

	            return handler;
	        }
	        return null;
	    }
	
    private class Handler{
        protected Object controller;//保存方法对应的实例
        protected Method method;//保存映射的方法
        protected Pattern pattern;
        protected Map<String, Integer> paramIndexMapping;//参数顺序


        protected Handler(Pattern pattern, Object controller, Method method){
            this.pattern = pattern;
            this.controller = controller;
            this.method = method;
            paramIndexMapping = new HashMap<>();
            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method) {

            //提取方法中加了注解的参数
            Annotation[][] pa = method.getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (Annotation a : pa[i]) {
                    if (a instanceof sRequestParam){
                        String paramName = ((sRequestParam)a).value();
                        if (!"".equals(paramName.trim())){
                            paramIndexMapping.put(paramName, i);
                        }
                    }
                }
            }

            //提取方法中的request和response参数
            Class<?>[] paramsTypes = method.getParameterTypes();
            for (int i = 0; i < paramsTypes.length; i++) {
                Class<?> type = paramsTypes[i];
                if (type == HttpServletRequest.class ||
                        type == HttpServletResponse.class){
                    paramIndexMapping.put(type.getName(), i);
                }
            }
        }

    }
	
}
