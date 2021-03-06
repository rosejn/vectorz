package mikera.vectorz;

import java.util.Arrays;

import static org.junit.Assert.*;
import mikera.vectorz.impl.SparseHashedVector;
import mikera.vectorz.impl.SparseIndexedVector;
import mikera.vectorz.impl.ZeroVector;

import org.junit.Test;

public class TestSparseVectors {

	@Test 
	public void testHashed() {
		SparseHashedVector v=SparseHashedVector.createLength(10);
		assertEquals(0,v.nonZeroCount());

		v.set(1,1);
		assertEquals(1.0,v.elementSum(),0.0);
		assertEquals(1,v.nonZeroCount());
		
	}
	
	@Test 
	public void testIndexed() {
		SparseIndexedVector v=SparseIndexedVector.createLength(10);
		
		assertEquals(0,v.nonZeroCount());

		v.set(1,1);
		assertEquals(1.0,v.elementSum(),0.0);
		assertEquals(1,v.nonZeroCount());
        assertTrue(Arrays.equals(new int[]{1},v.nonZeroIndices()));

        SparseIndexedVector w=v.clone();
        v.add(ZeroVector.create(10));
        assertEquals(w, v);

        SparseIndexedVector empty=SparseIndexedVector.createLength(3);
        SparseIndexedVector nonEmpty=SparseIndexedVector.create(Vector.of(1,0,2));
        empty.add(nonEmpty);
        assertEquals(Vector.of(1,0,2), empty);
	}
}
