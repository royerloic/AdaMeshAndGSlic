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
		result = prime * result + ((u == null || v == null) ? 0 : (u.hashCode()+v.hashCode()));
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

		if (other.u == u && other.v == v)
			return true;
		else if (other.u == v && other.v == u)
			return true;
		else
			return true;
	}

	@Override
	public String toString()
	{
		return "Edge [u=" + u + ", v=" + v + "]";
	}

}
