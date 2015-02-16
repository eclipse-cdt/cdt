package org.eclipse.cdt.utils.serial;

public enum BaudRate {

	B0(0),
	B50(50),
	B75(75),
	B110(110),
	B134(134),
	B150(150),
	B200(200),
	B300(300),
	B600(600),
	B1200(1200),
	B1800(1800),
	B2400(2400),
	B4800(4800),
	B7200(7200),
	B9600(9600),
	B14400(14400),
	B19200(19200),
	B28800(28800),
	B38400(38400),
	B57600(57600),
	B76800(76800),
	B115200(115200),
	B230400(230400);

	private final int rate;
	
	private BaudRate(int rate) {
		this.rate = rate;
	}
	
	public int getRate() {
		return rate;
	}

}
