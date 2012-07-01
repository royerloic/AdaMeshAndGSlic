package superpixel;

import gslic.LabelField;

public class SuperPixels
{
	final int dim;
	final int dimensions[];

	public SuperPixels(final int pDimension)
	{
		dim = pDimension;
		dimensions = new int[2];
	}
	
	public SuperPixels(int pWidth, int pHeight)
	{
		this(2);
		dimensions[0] = pWidth;
		dimensions[1] = pHeight;
	}
	
	public SuperPixels(final LabelField pLabeledField)
	{
		this(pLabeledField.width,pLabeledField.height);
		LabelFieldToSuperPixels.convert(pLabeledField,this);
	}





	
	
	
	
}
