package gslic;

public class LabelField
{
	final public int[] array;
	public final int width;
	public final int height;
	
	
	public LabelField(int pWidth, int pHeight)
	{
		super();
		width = pWidth;
		height = pHeight;
		array = new int[width*height];
	}
	
	
}
