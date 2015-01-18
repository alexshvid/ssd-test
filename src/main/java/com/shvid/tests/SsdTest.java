package com.shvid.tests;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SsdTest {

	private static final long ONE_GB = 1024L * 1024L * 1024L;
	
	public static void main(String[] args) throws Exception {

		System.out.println("SSD Test v1.0");

		if (args.length < 2) {
			System.out.println("Usage: ssd-test test_num size_gb");
			return;
		}

		Integer testNum = Integer.parseInt(args[0]);
		Integer sizeInGb = Integer.parseInt(args[1]);
		
		long size = ONE_GB * sizeInGb;
		
		switch(testNum) {
		
		case 1:
			testWriteNoProcessing(size);
			break;
		
		case 2:
			testWriteSimple(size);
			break;
		
		case 3:
			testInMemoryProcessing(size);
			break;
			
	  default:
				System.out.println("Unknown testNum " + testNum);
		}
		
	}

	private static void testWriteNoProcessing(final long fileSize) throws IOException {
		final File outFile = File.createTempFile("tmp", ".tmp");
		final long segments = fileSize / 1024;
		final byte[] buf = new byte[1024];
		// generate initial data
		new Random(1000).nextBytes(buf);
		// write durations
		final long[] writes = new long[(int) segments];

		final long start = System.currentTimeMillis();
		final OutputStream os = new BufferedOutputStream(new FileOutputStream(outFile), 32768);
		try {
			for (long count = 0; count < segments; ++count) {
				final long before = System.currentTimeMillis();
				os.write(buf);
				final long after = System.currentTimeMillis();
				writes[((int) count)] = after - before;
			}
		} finally {
			final long beforeClose = System.currentTimeMillis();
			os.close();
			final long time = System.currentTimeMillis();
			System.out.println("File size = " + outFile.length() / 1024 / 1024 / 1024 + " G");
			System.out.println("Time to close a file = " + ((time - beforeClose) / 1000.0) + " sec");
			System.out.println("Time to write a file with size = " + (fileSize / 1024 / 1024 / 1024) + " G = "
					+ ((time - start) / 1000.0) + " sec ");
			outFile.delete();
		}

		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		long total = 0;
		int cnt = 0;
		List<Long> lst = new ArrayList<Long>(1024);
		for (final long len : writes) {
			if (len != 0) {
				if (len < min)
					min = len;
				if (len > max)
					max = len;
				cnt++;
				total += len;
				lst.add(len);
			}
		}
		System.out.println("Expected count = " + writes.length / 32 + " Actual count = " + cnt);
		System.out.println("Min duration = " + min + " Max duration = " + max);
		System.out.println("Avg duration = " + (total / cnt));

		Collections.sort(lst);
		System.out.println("Median duration = " + lst.get(lst.size() / 2));
		System.out.println("75% duration = " + lst.get(lst.size() * 3 / 4));
		System.out.println("90% duration = " + lst.get(lst.size() * 9 / 10));
		System.out.println("95% duration = " + lst.get(lst.size() * 19 / 20));
		System.out.println("99% duration = " + lst.get(lst.size() * 99 / 100));

		for (int i = Math.max(0, lst.size()-10); i < lst.size(); ++i)
			if (lst.get(i) != 1)
				System.out.println("writes[" + i + "] = " + lst.get(i));
	}

	private static void testWriteSimple(final long fileSize) throws IOException {
		final File outFile = File.createTempFile("tmp", ".tmp");
		final long segments = fileSize / 1024;
		final byte[] buf = new byte[1024];
		// generate initial data
		new Random(1000).nextBytes(buf);
		// write durations
		final long[] writes = new long[(int) segments];

		final long start = System.currentTimeMillis();
		final OutputStream os = new BufferedOutputStream(new FileOutputStream(outFile), 32768);
		try {
			for (long count = 0; count < segments; ++count) {
				// some calculation before each write
				for (int i = 0; i < 1024; ++i)
					buf[i] = (byte) (buf[i] * buf[i] / 3);
				final long before = System.currentTimeMillis();
				os.write(buf);
				final long after = System.currentTimeMillis();
				writes[((int) count)] = after - before;
			}
		} finally {
			final long beforeClose = System.currentTimeMillis();
			os.close();
			final long time = System.currentTimeMillis();
			System.out.println("File size = " + outFile.length() / 1024 / 1024 / 1024 + " G");
			System.out.println("Time to close a file = " + ((time - beforeClose) / 1000.0) + " sec");
			System.out.println("Time to write a file with size = " + (fileSize / 1024 / 1024 / 1024) + " G = "
					+ ((time - start) / 1000.0) + " sec ");
			outFile.delete();
		}

		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		long total = 0;
		int cnt = 0;
		List<Long> lst = new ArrayList<Long>(1024);
		for (final long len : writes) {
			if (len != 0) {
				if (len < min)
					min = len;
				if (len > max)
					max = len;
				cnt++;
				total += len;
				lst.add(len);
			}
		}
		System.out.println("Expected count = " + writes.length / 32 + " Actual count = " + cnt);
		System.out.println("Min duration = " + min + " Max duration = " + max);
		System.out.println("Avg duration = " + (total / cnt));

		Collections.sort(lst);
		System.out.println("Median duration = " + lst.get(lst.size() / 2));
		System.out.println("75% duration = " + lst.get(lst.size() * 3 / 4));
		System.out.println("90% duration = " + lst.get(lst.size() * 9 / 10));
		System.out.println("95% duration = " + lst.get(lst.size() * 19 / 20));
		System.out.println("99% duration = " + lst.get(lst.size() * 99 / 100));

		for (int i =  Math.max(0, lst.size()-10); i < lst.size(); ++i)
			if (lst.get(i) != 1)
				System.out.println("writes[" + i + "] = " + lst.get(i));
	}

	private static void testInMemoryProcessing(final long fileSize) throws IOException {
		final long segments = fileSize / 1024;
		final byte[] buf = new byte[1024];
		// generate initial data
		new Random(1000).nextBytes(buf);

		long sum = 0;
		final long start = System.currentTimeMillis();
		try {
			for (long count = 0; count < segments; ++count) {
				// some calculation before each write
				for (int i = 0; i < 1024; ++i)
					buf[i] = (byte) (buf[i] * buf[i] / 3);
				for (int i = 0; i < 1024; ++i)
					sum += buf[i];
			}
		} finally {
			System.out.println(sum);
			final long time = System.currentTimeMillis();
			System.out.println("Time to generate data for a file with size = " + (fileSize / 1024 / 1024 / 1024) + " G = "
					+ ((time - start) / 1000.0) + " sec ");
		}
	}

}
