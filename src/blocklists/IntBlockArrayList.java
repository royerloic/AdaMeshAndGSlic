package blocklists;

import gnu.trove.list.array.TIntArrayList;

public class IntBlockArrayList extends BlockArrayList
{
	TIntArrayList mIntList;

	public IntBlockArrayList(int pBlocksize, int pInitialCapacity)
	{
		super();
		blocksize = pBlocksize;
		mIntList = new TIntArrayList(pInitialCapacity * pBlocksize);
	}

	public IntBlockArrayList(int pBlocksize)
	{
		super();
		blocksize = pBlocksize;
		length = 0;
		mIntList = new TIntArrayList();
	}

	public IntBlockArrayList(IntBlockArrayList pManagedIntBlockArray)
	{
		blocksize = pManagedIntBlockArray.blocksize;
		mIntList = new TIntArrayList(pManagedIntBlockArray.mIntList);
	}

	public int add(final int... pBlock)
	{
		if (pBlock.length != blocksize)
			throw new ArrayStoreException("Wrong block size!");
		mIntList.add(pBlock);
		return length++;
	}

	public void set(final int pBlockIndex, final int[] pBlock)
	{
		mIntList.set(pBlockIndex * blocksize, pBlock);
	}

	public void set(final int pBlockIndex,
									final int pIndexInBlock,
									final int pValue)
	{
		mIntList.setQuick(pBlockIndex * blocksize + pIndexInBlock, pValue);
	}

	public int[] get(int[] pStorageArray, final int pBlockIndex)
	{
		if (pStorageArray == null || pStorageArray.length != mIntList.size())
		{
			pStorageArray = new int[blocksize];
		}

		pStorageArray = mIntList.toArray(pStorageArray);

		return pStorageArray;
	}

	public final int get(final int pBlockIndex, int pIndexInBlock)
	{
		return mIntList.getQuick(pBlockIndex * blocksize + pIndexInBlock);
	}

	public void remove(final int pBlockIndex)
	{
		mIntList.remove(pBlockIndex, blocksize);
	}

	public int[] getArray(int[] pArray)
	{
		if (pArray == null || pArray.length != mIntList.size())
		{
			pArray = new int[mIntList.size()];
		}

		return mIntList.toArray(pArray);
	}

	public void clear()
	{
		mIntList.clear();
	}
}
