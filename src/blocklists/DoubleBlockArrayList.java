package blocklists;

import gnu.trove.list.array.TDoubleArrayList;

public class DoubleBlockArrayList extends BlockArrayList
{
	TDoubleArrayList mDoubleList;

	public DoubleBlockArrayList(int pBlocksize, int pInitialCapacity)
	{
		super();
		blocksize = pBlocksize;
		mDoubleList = new TDoubleArrayList(pInitialCapacity * pBlocksize);
	}

	public DoubleBlockArrayList(int pBlocksize)
	{
		super();
		blocksize = pBlocksize;
		length = 0;
		mDoubleList = new TDoubleArrayList();
	}

	public DoubleBlockArrayList(DoubleBlockArrayList pManagedDoubleBlockArray)
	{
		blocksize = pManagedDoubleBlockArray.blocksize;
		mDoubleList = new TDoubleArrayList(pManagedDoubleBlockArray.mDoubleList);
	}

	public int add(final double... pBlock)
	{
		if (pBlock.length != blocksize)
			throw new ArrayStoreException("Wrong block size!");
		mDoubleList.add(pBlock);
		return length++;
	}

	public void set(final int pBlockIndex, final double[] pBlock)
	{
		mDoubleList.set(pBlockIndex * blocksize, pBlock);
	}

	public void set(final int pBlockIndex,
									final int pIndexInBlock,
									final double pValue)
	{
		mDoubleList.setQuick(	pBlockIndex * blocksize + pIndexInBlock,
													pValue);
	}

	public double[] get(double[] pStorageArray, final int pBlockIndex)
	{
		if (pStorageArray == null || pStorageArray.length != blocksize)
		{
			pStorageArray = new double[blocksize];
		}

		pStorageArray = mDoubleList.toArray(pStorageArray);

		return pStorageArray;
	}

	public final double get(final int pBlockIndex, int pIndexInBlock)
	{
		return mDoubleList.getQuick(pBlockIndex * blocksize
																+ pIndexInBlock);
	}

	public void remove(final int pBlockIndex)
	{
		mDoubleList.remove(pBlockIndex, blocksize);
	}

	public double[] getArray(double[] pArray)
	{
		if (pArray == null || pArray.length != mDoubleList.size())
		{
			pArray = new double[mDoubleList.size()];
		}

		return mDoubleList.toArray(pArray);
	}

	public void set(DoubleBlockArrayList pDoubleBlockArrayList)
	{
		mDoubleList = pDoubleBlockArrayList.mDoubleList;
	}

	public void clear()
	{
		mDoubleList.clear();
	}

}
