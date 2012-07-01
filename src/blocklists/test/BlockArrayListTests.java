package blocklists.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Random;

import org.junit.Test;

import blocklists.DoubleBlockArrayList;
import blocklists.IntBlockArrayList;

public class BlockArrayListTests
{
	Random rnd = new Random();

	@Test
	public void testDouble()
	{

		final DoubleBlockArrayList list = new DoubleBlockArrayList(3);

		for (int i = 0; i < 10; i++)
		{
			list.add(0, 1, 2);
		}

		for (int i = 0; i < 10; i++)
		{
			assertEquals(0.0, list.get(i, 0), 0.01);
			assertEquals(1.0, list.get(i, 1), 0.01);
			assertEquals(2.0, list.get(i, 2), 0.01);
		}

		list.clear();

		for (int i = 0; i < 10; i++)
		{
			list.set(i, new double[]
			{ 0, 1, 2 });
		}

		for (int i = 0; i < 10; i++)
		{
			assertEquals(0.0, list.get(i, 0), 0.01);
			assertEquals(1.0, list.get(i, 1), 0.01);
			assertEquals(2.0, list.get(i, 2), 0.01);
		}

		System.out.println(list.toString());

	}

	@Test
	public void testInt()
	{

		final IntBlockArrayList list = new IntBlockArrayList(3);

		for (int i = 0; i < 10; i++)
		{
			list.add(0, 1, 2);
		}

		for (int i = 0; i < 10; i++)
		{
			assertSame(0, list.get(i, 0));
			assertSame(1, list.get(i, 1));
			assertSame(2, list.get(i, 2));
		}

		System.out.println(list.toString());

	}

}
