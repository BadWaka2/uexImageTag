package org.zywx.wbpalmstar.plugin.ueximagetag.utils;

/**
 * 容错修正工具类
 * 
 * @author waka
 * @version createTime:2016年5月18日 上午11:27:23
 */
public class FormatAmendUtil {

	/**
	 * 在0和1之间
	 * 
	 * @param x
	 * @return
	 */
	public static float between0and1(float x) {
		if (x < 0) {
			x = 0;
		} else if (x > 1) {
			x = 1;
		}
		return x;
	}

	/**
	 * 判断是否是颜色字符串
	 * 
	 * @param colorStr
	 * @return
	 */
	public static boolean isColorStr(String colorStr) {

		// 判断颜色格式，第一道关卡，字符数不等于7或9的直接return;
		if (!(colorStr.length() == 7 || colorStr.length() == 9)) {
			return false;
		}

		// 如果第一位不是'#'，return
		if (colorStr.charAt(0) != '#') {
			return false;
		}

		// 如果是7位颜色代码，则增加透明度代码'ff'
		if (colorStr.length() == 7) {
			StringBuffer stringBuffer = new StringBuffer(colorStr);
			stringBuffer.insert(1, 'f');
			stringBuffer.insert(2, 'f');
			colorStr = stringBuffer.toString();
		}

		// 判断颜色格式，第二道关卡，每个字符的ASCII码必须在48~57(0~9的ASCII码)或65~70(A~F的ASCII码)或97~102(a~f的ASCII码)之间
		for (int i = 1; i < 7; i++) {
			char c = colorStr.charAt(i);// 得到每个字符
			int ascii_c = c;// 获得该字符ascii码
			// 如果该字符不在规定范围内，return
			if (ascii_c < 48 || (ascii_c > 57 && ascii_c < 65) || (ascii_c > 70 && ascii_c < 97) || ascii_c > 102) {
				return false;
			}
		}

		return true;

	}

}
