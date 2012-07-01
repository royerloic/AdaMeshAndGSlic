package maps.test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import maps.IntIntSetMap;

import org.junit.Test;

public class IntIntSetMapTests
{
	Random rnd = new Random();

	@Test
	public void test1()
	{

		final IntIntSetMap map = new IntIntSetMap();

		for (int i = 0; i < 1000; i++)
		{
			map.add(i, i);
			map.add(i, i + 1);
			map.add(i, i + 2);
		}

		for (int i = 0; i < 1000; i++)
		{
			assertSame(3, map.get(i).size());
			assertTrue(map.contains(i, i));
			assertTrue(map.contains(i, i));
			assertTrue(map.contains(i, i));
		}

		System.out.println(map.toString());

	}

	@Test
	public void test2()
	{

		final IntIntSetMap map = new IntIntSetMap();

		for (int i = 0; i < 1000; i++)
		{
			map.add(rnd.nextInt(1000), i);
		}

		for (int i = 0; i < 1000; i++)
		{
			assertTrue(map.contains(i));
		}

		System.out.println(map.toString());

	}

}
