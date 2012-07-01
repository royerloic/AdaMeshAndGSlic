package mesh;

public class Edge
{
	public final Vertex u;
	public final Vertex v;
	public double stress;
	
	
	public Edge(Vertex pU, Vertex pV)
	{
		super();
		u = pU;
		v = pV;
	}

	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (u == null)
		{
			if (other.u != null)
				return false;
		}
		else if (!u.equals(other.u))
			return false;
		if (v == null)
		{
			if (other.v != null)
				return false;
		}
		else if (!v.equals(other.v))
			return false;
		return true;
	}




	@Override
	public String toString()
	{
		return "Edge [u=" + u + ", v=" + v + "]";
	}
	
	
}
