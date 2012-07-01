package blocklists;

public class BlockArrayList
{

	protected int blocksize;
	protected int length = 0;

	public BlockArrayList()
	{
		super();
	}

	public int getNumberOfBlocks()
	{
		return length;
	}

}