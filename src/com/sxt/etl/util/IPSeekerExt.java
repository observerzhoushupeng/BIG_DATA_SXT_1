package com.sxt.etl.util;

import com.sxt.common.GlobalConstants;
import com.sxt.etl.util.ip.IPSeeker;

/**
 * ��������ip�������࣬���յ���IpSeeker��(����)<br/>
 * ����ip���յķ���ʱ���������� ʡ������ ��������<br/>
 * ����ǹ����ip����ôֱ������Ϊunknown unknown unknown<br/>
 * ����ǹ���ip�����û�����н�������ô������Ϊ�й� unknown unknown<br/>
 * 
 * @author root
 *
 */
public class IPSeekerExt extends IPSeeker {
	private RegionInfo DEFAULT_INFO = new RegionInfo();

	/**
	 * ����ip��ַ�����ظ�ip��ַ��Ӧ�Ĺ���ʡ����Ϣ<br/>
	 * �����ip����ʧ�ܣ���ôֱ�ӷ���Ĭ��ֵ
	 * 
	 * @param ip
	 *            Ҫ������ip��ַ����ʽΪ: 120.197.87.216
	 * @return
	 */
	public RegionInfo analyticIp(String ip) {
		if (ip == null || ip.trim().isEmpty()) {
			return DEFAULT_INFO;
		}

		RegionInfo info = new RegionInfo();
		try {
			String country = super.getCountry(ip);
			if ("������".equals(country)) {
				info.setCountry("�й�");
				info.setProvince("�Ϻ���");
			} else if (country != null && !country.trim().isEmpty()) {
				// ��ʾ��ip��һ�����Խ�����ip
				country = country.trim();
				int length = country.length();
				int index = country.indexOf('ʡ');
				if (index > 0) {
					// ��ǰip����23��ʡ֮���һ����country�ĸ�ʽΪ��xxxʡ(xxx��)(xxx��/��)
					info.setCountry("�й�");
					if (index == length - 1) {
						info.setProvince(country); // ����ʡ�ݣ���ʽ���룺 �㶫ʡ
					} else {
						// ��ʽΪ���㶫ʡ������
						info.setProvince(country.substring(0, index + 1)); // ����ʡ��
						int index2 = country.indexOf('��', index); // �鿴��һ�������е�λ��
						if (index2 > 0) {
							country.substring(1, 1);
							info.setCity(country.substring(index + 1,
									Math.min(index2 + 1, length))); // ����city
						}
					}
				} else {
					// ��������������� �ĸ�ֱϽ�� 2���ر�������
					String flag = country.substring(0, 2); // ���ַ���ǰ��λ
					switch (flag) {
					case "����":
						info.setCountry("�й�");
						info.setProvince("���ɹ�������");
						country = country.substring(3);
						if (country != null && !country.isEmpty()) {
							index = country.indexOf('��');
							if (index > 0) {
								info.setCity(country.substring(0,
										Math.min(index + 1, country.length()))); // ������
							}
						}
						break;
					case "����":
					case "����":
					case "����":
					case "�½�":
						info.setCountry("�й�");
						info.setProvince(flag);
						country = country.substring(2);
						if (country != null && !country.isEmpty()) {
							index = country.indexOf('��');
							if (index > 0) {
								info.setCity(country.substring(0,
										Math.min(index + 1, country.length()))); // ������
							}
						}
						break;
					case "�Ϻ�":
					case "����":
					case "���":
					case "����":
						info.setCountry("�й�");
						info.setProvince(flag + "��");
						country = country.substring(3); // ȥ�����ʡ��/ֱϽ��
						if (country != null && !country.isEmpty()) {
							index = country.indexOf('��');
							if (index > 0) {
								char ch = country.charAt(index - 1);
								if (ch != 'У' || ch != 'С') {
									info.setCity(country.substring(
											0,
											Math.min(index + 1,
													country.length()))); // ������
								}
							}

							if (RegionInfo.DEFAULT_VALUE.equals(info.getCity())) {
								// city����Ĭ��ֵ
								index = country.indexOf('��');
								if (index > 0) {
									info.setCity(country.substring(
											0,
											Math.min(index + 1,
													country.length()))); // ������
								}
							}
						}
						break;
					case "���":
					case "����":
						info.setCountry("�й�");
						info.setProvince(flag + "�ر�������");
						break;
					default:
						break;
					}
				}
			}
		} catch (Exception e) {
			// ���������г����쳣
			e.printStackTrace();
		}
		return info;
	}

	/**
	 * ip������ص�һ��model
	 * 
	 * @author root
	 *
	 */
	public static class RegionInfo {
		public static final String DEFAULT_VALUE = GlobalConstants.DEFAULT_VALUE; // Ĭ��ֵ
		private String country = DEFAULT_VALUE; // ����
		private String province = DEFAULT_VALUE; // ʡ��
		private String city = DEFAULT_VALUE; // ����

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getProvince() {
			return province;
		}

		public void setProvince(String province) {
			this.province = province;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		@Override
		public String toString() {
			return "RegionInfo [country=" + country + ", province=" + province
					+ ", city=" + city + "]";
		}
	}
}
