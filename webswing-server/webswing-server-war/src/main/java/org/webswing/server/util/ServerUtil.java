package org.webswing.server.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.model.MsgOut;
import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.model.c2s.InputEventsFrameMsgIn;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.server.common.model.SwingConfig;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.common.util.WebswingObjectMapper;
import org.webswing.server.services.websocket.WebSocketConnection;

public class ServerUtil {
	private static final Logger log = LoggerFactory.getLogger(ServerUtil.class);
	private static final ProtoMapper protoMapper = new ProtoMapper();

	public static String encode2Json(MsgOut m) {
		try {
			return WebswingObjectMapper.get().writeValueAsString(m);
		} catch (IOException e) {
			log.error("Json encoding object failed: " + m, e);
			return null;
		}
	}

	public static byte[] encode2Proto(MsgOut m) {
		try {
			return protoMapper.encodeProto(m);
		} catch (IOException e) {
			log.error("Proto encoding object failed: " + m, e);
			return null;
		}
	}

	public static Object decodeJson(String s) {
		Object o = null;
		Class<?>[] classes = new Class<?>[] { InputEventsFrameMsgIn.class };
		for (Class<?> c : classes) {
			try {
				o = WebswingObjectMapper.get().readValue(s, c);
				break;
			} catch (IOException e) {
				log.error("Failed to decode json message:", e);
			}
		}
		return o;
	}

	public static Object decodeProto(byte[] message) {
		Object o = null;
		Class<?>[] classes = new Class<?>[] { InputEventsFrameMsgIn.class };
		for (Class<?> c : classes) {
			try {
				o = protoMapper.decodeProto(message, c);
				break;
			} catch (IOException e) {
				log.error("Failed to decode proto message:", e);
			}
		}
		return o;
	}

	public static AppFrameMsgOut decodePlaybackProto(byte[] message) {
		try {
			return protoMapper.decodeProto(message, AppFrameMsgOut.class);
		} catch (IOException e) {
			log.error("Failed to decode proto message:", e);
			return null;
		}
	}

	public static String getClientIp(WebSocketConnection r) {
		return r == null ? null : r.getRequest().getRemoteAddr();
	}

	public static String getClientOs(WebSocketConnection r) {
		if (r != null) {
			String userAgent = r.getRequest().getHeader("User-Agent");
			if (userAgent.toLowerCase().indexOf("windows") >= 0) {
				return "Windows";
			} else if (userAgent.toLowerCase().indexOf("mac") >= 0) {
				return "Mac";
			} else if (userAgent.toLowerCase().indexOf("x11") >= 0) {
				return "Linux";
			} else if (userAgent.toLowerCase().indexOf("android") >= 0) {
				return "Android";
			} else if (userAgent.toLowerCase().indexOf("iphone") >= 0) {
				return "IPhone";
			} else {
				return "Unknown";
			}
		}
		return null;
	}

	public static String getClientBrowser(WebSocketConnection r) {
		if (r != null) {

			String userAgent = r.getRequest().getHeader("User-Agent");
			String user = userAgent.toLowerCase();
			String browser = "Unknown";
			if (user.contains("msie")) {
				String substring = userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
				browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
			} else if (user.contains("safari") && user.contains("version")) {
				browser = (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0] + "-" + (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
			} else if (user.contains("opr") || user.contains("opera")) {
				if (user.contains("opera"))
					browser = (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0] + "-" + (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
				else if (user.contains("opr"))
					browser = ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-")).replace("OPR", "Opera");
			} else if (user.contains("chrome")) {
				browser = (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
			} else if (user.contains("firefox")) {
				browser = (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
			}
			return browser;
		}
		return null;
	}

	public static String resolveInstanceIdForMode(WebSocketConnection r, ConnectionHandshakeMsgIn h, SwingConfig conf) {
		if (h.isMirrored()) {
			return h.getClientId();
		} else {
			switch (conf.getSessionMode()) {
			case ALWAYS_NEW_SESSION:
				return h.getClientId() + h.getViewId();
			case CONTINUE_FOR_BROWSER:
				return h.getClientId();
			case CONTINUE_FOR_USER:
				return r.getUser() != null ? r.getUser().getUserId() : "null";
			default:
				return h.getClientId();
			}
		}
	}

	public static boolean isRecording(HttpServletRequest r) {
		String recording = (String) r.getHeader(Constants.HTTP_ATTR_RECORDING_FLAG);
		return Boolean.parseBoolean(recording);
	}

	public static String getCustomArgs(HttpServletRequest r) {
		String args = (String) r.getHeader(Constants.HTTP_ATTR_ARGS);
		return args != null ? args : "";
	}

	public static int getDebugPort(HttpServletRequest r) {
		String recording = (String) r.getHeader(Constants.HTTP_ATTR_DEBUG_PORT);
		try {
			return Integer.parseInt(recording);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static URL getWebResource(String resource, ServletContext servletContext, File webFolder) {
		URL result = null;
		if (webFolder != null && webFolder.isDirectory()) {
			File file = new File(webFolder, resource);
			if (file.isFile()) {
				try {
					result = file.toURI().toURL();
				} catch (MalformedURLException e) {
					log.error("Failed to get file from webFolder.", e);
				}
			}
		}
		if (result == null) {
			try {
				result = servletContext.getResource(resource);
			} catch (MalformedURLException e) {
				log.error("Failed to get file from Web context path.", e);
			}
		}
		return result;
	}

	public static boolean isFileLocked(File file) {
		if (file.exists()) {
			try {
				Path source = file.toPath();
				Path dest = file.toPath().resolveSibling(file.getName() + ".wstest");
				Files.move(source, dest);
				Files.move(dest, source);
				return false;
			} catch (IOException e) {
				return true;
			}
		}
		return false;
	}

	public static String getContextPath(ServletContext ctx) {
		String contextPath = ctx.getContextPath();
		String contextPathExplicit = System.getProperty(Constants.REVERSE_PROXY_CONTEXT_PATH);
		if (contextPathExplicit != null) {
			return CommonUtil.toPath(contextPathExplicit);
		} else if (contextPath != null && !contextPath.equals("/") && !contextPath.equals("")) {
			return CommonUtil.toPath(contextPath);
		} else {
			return "";
		}
	}
}
