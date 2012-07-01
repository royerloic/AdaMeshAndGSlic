package maps;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;

public class IntIntSetMap
{
	private final TIntObjectHashMap<TIntHashSet> mSetMap;

	public IntIntSetMap(int pInitialCapacity)
	{
		super();
		mSetMap = new TIntObjectHashMap<TIntHashSet>(pInitialCapacity);
	}

	public IntIntSetMap()
	{
		super();
		mSetMap = new TIntObjectHashMap<TIntHashSet>();
	}

	public IntIntSetMap(IntIntSetMap pIntIntSetMap)
	{
		mSetMap = new TIntObjectHashMap<TIntHashSet>(pIntIntSetMap.mSetMap);
	}

	public TIntHashSet add(final int pIndex)
	{
		TIntHashSet set = mSetMap.get(pIndex);
		if (set == null)
		{
			set = new TIntHashSet();
			mSetMap.put(pIndex, set);
		}
		return set;
	}

	public void add(final int pIndex, final int pValue)
	{
		add(pIndex).add(pValue);
	}

	public void put(final int pIndex, int... pValues)
	{
		for (final int lValue : pValues)
		{
			add(pIndex, lValue);
		}
	}

	public void remove(final int pIndex)
	{
		mSetMap.remove(pIndex);
	}
	
	public void remove(final int pIndex, final int pValue)
	{
		final TIntHashSet set = mSetMap.get(pIndex);
		if (set != null)
		{
			set.remove(pValue);
		}
	}

	public final TIntHashSet get(final int pIndex)
	{
		return mSetMap.get(pIndex);
	}

	public final boolean contains(final int pIndex, final int pValue)
	{
		final TIntHashSet set = mSetMap.get(pIndex);
		if (set == null)
			return false;
		return set.contains(pValue);
	}

	public boolean contains(int pValue)
	{
		final Object[] lValues = mSetMap.values();
		for (final Object set : lValues)
			if (set != null)
			{
				if (((TIntHashSet) set).contains(pValue))
					return true;
			}
		return false;
	}

	public final void clear()
	{
		mSetMap.forEachValue(new TObjectProcedure<TIntHashSet>()
		{

			@Override
			public boolean execute(TIntHashSet pTIntHashSet)
			{
				pTIntHashSet.clear();
				return true;
			}
		});

	}

	public int[] keys()
	{
		return mSetMap.keys();
	}

	public TIntHashSet[] values()
	{
		return (TIntHashSet[]) mSetMap.values();
	}

	public int size()
	{
		return mSetMap.size();
	}

	@Override
	public String toString()
	{
		final StringBuffer lStringBuffer = new StringBuffer();
		for (int i = 0; i < mSetMap.size(); i++)
		{
			final TIntHashSet set = mSetMap.get(i);
			lStringBuffer.append(i);
			lStringBuffer.append("->");
			if (set == null)
			{
				lStringBuffer.append("{}");
			}
			else
			{
				lStringBuffer.append(set);
			}
			lStringBuffer.append("\n");
		}
		return lStringBuffer.toString();
	}

}
