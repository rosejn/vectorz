package mikera.matrixx;

import java.io.StringReader;
import java.util.List;

import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Parsers;
import mikera.matrixx.impl.DiagonalMatrix;
import mikera.matrixx.impl.IdentityMatrix;
import mikera.util.Rand;
import mikera.vectorz.AVector;
import mikera.vectorz.Tools;
import mikera.vectorz.Vector3;
import mikera.vectorz.util.VectorzException;

/**
 * Static method class for matrices
 * 
 * @author Mike
 */
public class Matrixx {

	public static IdentityMatrix createIdentityMatrix(int dimensions) {
		return new IdentityMatrix(dimensions);
	}
	
	public static DiagonalMatrix createScaleMatrix(int dimensions, double factor) {
		DiagonalMatrix im=new DiagonalMatrix(dimensions);
		for (int i=0; i<dimensions; i++) {
			im.set(i,i,factor);
		}
		return im;
	}
	
	public static DiagonalMatrix createScaleMatrix(double... scalingFactors) {
		int dimensions=scalingFactors.length;
		DiagonalMatrix im=new DiagonalMatrix(dimensions);
		for (int i=0; i<dimensions; i++) {
			im.set(i,i,scalingFactors[i]);
		}
		return im;
	}
	
	public static Matrix22 create2DRotationMatrix(double angle) {
		double sa=Math.sin(angle);
		double ca=Math.cos(angle);
		return new Matrix22(
				ca,-sa,
				sa, ca);
	}
	
	public static Matrix33 createRotationMatrix(Vector3 axis, double angle) {
		return createRotationMatrix(axis.x,axis.y,axis.z,angle);
	}
	
	public static Matrix33 createRotationMatrix(double x, double y, double z, double angle) {
		double d=Math.sqrt(x*x+y*y+z*z);
		double u=x/d;
		double v=y/d;
		double w=z/d;
		double ca=Math.cos(angle);
		double sa=Math.sin(angle);
		return new Matrix33(
				u*u+(1-u*u)*ca , u*v*(1-ca)-w*sa , u*w*(1-ca) + v*sa,
				u*v*(1-ca)+w*sa, v*v+(1-v*v)*ca  , v*w*(1-ca) - u*sa,
				u*w*(1-ca)-v*sa, v*w*(1-ca)+u*sa , w*w+(1-w*w)*ca);
	}
	
	public static Matrix33 createRotationMatrix(AVector v, double angle) {
		if (!(v.length()==3)) throw new VectorzException("Rotation matrix requires a 3d axis vector");
		return createRotationMatrix(v.get(0),v.get(1),v.get(2),angle);
	}
	
	public static Matrix33 createXAxisRotationMatrix(double angle) {
		return createRotationMatrix(1,0,0,angle);
	}
	
	public static Matrix33 createYAxisRotationMatrix(double angle) {
		return createRotationMatrix(0,1,0,angle);
	}

	public static Matrix33 createZAxisRotationMatrix(double angle) {
		return createRotationMatrix(0,0,1,angle);
	}

	
	public static AMatrix createRandomSquareMatrix(int dimensions) {
		AMatrix m=createSquareMatrix(dimensions);
		fillRandomValues(m);
		return m;
	}
	
	public static AMatrix createRandomMatrix(int rows, int columns) {
		AMatrix m=newMatrix(rows,columns);
		fillRandomValues(m);
		return m;
	}

	static MatrixMN createInverse(AMatrix m) {
		if (m.rowCount() != m.columnCount()) {
			throw new IllegalArgumentException("Matrix must be square for inverse!");
		}

		int dims = m.rowCount();

		MatrixMN am = new MatrixMN(m);
		int[] rowPermutations = new int[dims];

		// perform LU-based inverse on matrix
		decomposeLU(am, rowPermutations);
		return backSubstituteLU(am, rowPermutations);
	}
	
	/**
	 * Computes LU decomposition of a matrix, returns true if
	 * successful (i.e. if matrix is non-singular)
	 */
	private static void decomposeLU(MatrixMN am, int[] permutations) {
		int dims = permutations.length;
		double[] data=am.data;

		double rowFactors[] = new double[dims];
		calcRowFactors(data, rowFactors);

		for (int col = 0; col < dims; col++) {
			// Scan upper diagonal matrix
			for (int row = 0; row < col; row++) {
				int dataIndex = (dims * row) + col;
				double acc = data[dataIndex];
				for (int i = 0; i < row; i++) {
					acc -= data[(dims * row) + i] * data[(dims * i) + col];
				}
				data[dataIndex] = acc;
			}

			// Find index of largest pivot
			int maxIndex = 0;
			double maxValue = Double.NEGATIVE_INFINITY;
			for (int row = col; row < dims; row++) {
				int dataIndex = (dims * row) + col;
				double acc = data[dataIndex];
				for (int i = 0; i < col; i++) {
					acc -= data[(dims * row) + i] * data[(dims * i) + col];
				}
				data[dataIndex] = acc;

				double value = rowFactors[row] * Math.abs(acc);
				if (value > maxValue) {
					maxValue = value;
					maxIndex = row;
				}
			}

			if (col != maxIndex) {
				am.swapRows(col,maxIndex);
				rowFactors[maxIndex] = rowFactors[col];
			}

			permutations[col] = maxIndex;

			if (data[(dims * col) + col] == 0.0) {
				throw new VectorzException(
						"Matrix is singular, cannot compute inverse!");
			}

			// Scale lower diagonal matrix using values on diagonal
			double diagonalValue = data[(dims * col) + col];
			double factor = 1.0 / diagonalValue;
			int offset = dims * (col + 1) + col;
			for (int i = 0; i < ((dims - 1) - col); i++) {
				data[(dims * i) + offset] *= factor;
			}
		}
	}

	/**
	 * Utility function to calculate scale factors for each row
	 */
	private static void calcRowFactors(double[] data, double[] factorsOut) {
		int dims = factorsOut.length;
		for (int row = 0; row < dims; row++) {
			double maxValue = 0.0;

			// find maximum value in the row
			for (int col = 0; col < dims; col++) {
				maxValue = Math.max(maxValue, Math.abs(data[row * dims + col]));
			}

			if (maxValue == 0.0) {
				throw new VectorzException("Matrix is singular!");
			}

			// scale factor for row should reduce maximum absolute value to 1.0
			factorsOut[row] = 1.0 / maxValue;
		}
	}

	private static MatrixMN backSubstituteLU(MatrixMN am, int[] permutations) {
		int dims = permutations.length;
		double[] dataIn=am.data;
		
		// create identity matrix in output
		MatrixMN result=new MatrixMN(Matrixx.createIdentityMatrix(dims));
		double[] dataOut = result.data;

		for (int col = 0; col < dims; col++) {
			int rowIndex = -1;

			// Forward substitution phase
			for (int row = 0; row < dims; row++) {
				int pRow = permutations[row];
				double acc = dataOut[(dims * pRow) + col];
				dataOut[(dims * pRow) + col] = dataOut[(dims * row) + col];
				if (rowIndex >= 0) {
					for (int i = rowIndex; i <= row - 1; i++) {
						acc -= dataIn[(row * dims) + i]
								* dataOut[(dims * i) + col];
					}
				} else if (acc != 0.0) {
					rowIndex = row;
				}
				dataOut[(dims * row) + col] = acc;
			}

			// Back substitution phase
			for (int row = 0; row < dims; row++) {
				int irow = (dims - 1 - row);
				int offset = dims * irow;
				double total = 0.0;
				for (int i = 0; i < row; i++) {
					total += dataIn[offset + ((dims - 1) - i)]
							* dataOut[(dims * ((dims - 1) - i)) + col];
				}
				double diagonalValue = dataIn[offset + irow];
				dataOut[(dims * irow) + col] = (dataOut[(dims * irow) + col] - total)
						/ diagonalValue;
			}
		}
		
		return result;
	}

	
	/**
	 * Creates an empty (zero-filled) mutable matrix of the specified size
	 * 
	 * @param rows
	 * @param columns
	 * @return
	 */
	public static AMatrix newMatrix(int rows, int columns) {
		if ((rows==columns)) {
			if (rows==3) return new Matrix33();
			if (rows==2) return new Matrix22();
		}
		return new MatrixMN(rows,columns);
	}
	
	public static AMatrix createFromVector(AVector data, int rows, int columns) {
		assert(data.length()==rows*columns);
		AMatrix m=newMatrix(rows, columns);
		for (int i=0; i<rows; i++) {
			for (int j=0; j<columns; j++) {
				m.set(i,j,data.get(i*columns+j));
			}
		}
		return m;
	}

	private static AMatrix createSquareMatrix(int dimensions) {
		switch (dimensions) {
		case 2: return new Matrix22();
		case 3: return new Matrix33();
		default: return newMatrix(dimensions,dimensions);
		}
	}

	public static AMatrix create(AMatrix m) {
		int rows=m.rowCount();
		int columns=m.columnCount();
		if (rows==columns) {
			if (rows==3) {
				return new Matrix33(m);
			} else if (rows==2) {
				return new Matrix22(m);
			}				
		}
		return new MatrixMN(m);
	}
	
	public static AMatrix create(IMatrix m) {
		int rows=m.rowCount();
		int columns=m.columnCount();
		AMatrix result=newMatrix(rows,columns);
		for (int i=0; i<rows; i++) {
			for (int j=0; j<columns; j++) {
				result.set(i,j,m.get(i, j));
			}
		}
		return result;
	}

	public static void fillRandomValues(AMatrix m) {
		int rows=m.rowCount();
		int columns=m.columnCount();
		for (int i=0; i<rows; i++) {
			for (int j=0; j<columns; j++) {
				m.set(i,j,Rand.nextDouble());
			}
		}
	}

	public static AMatrix createFromVectors(AVector... data) {
		int rc=data.length;
		int cc=(rc==0)?0:data[0].length();
		AMatrix m=newMatrix(rc,cc);
		for (int i=0; i<rc; i++) {
			m.getRow(i).set(data[i]);
		}
		return m;
	}
	
	// ====================================
	// Edn formatting and parsing functions

	private static Parser.Config getMatrixParserConfig() {
		return Parsers.defaultConfiguration();
	}
	
	/**
	 * Parse a matrix in edn format
	 * @param ednString
	 * @return
	 */
	public static AMatrix parse(String ednString) {
		Parser p=Parsers.newParser(getMatrixParserConfig(),new StringReader(ednString));
		@SuppressWarnings("unchecked")
		List<List<Object>> data=(List<List<Object>>) p.nextValue();
		int rc=data.size();
		int cc=(rc==0)?0:data.get(0).size();
		AMatrix m=newMatrix(rc,cc);
		for (int i=0; i<rc; i++) {
			for (int j=0; j<cc; j++) {
				m.set(i,j,Tools.toDouble(data.get(i).get(j)));
			}
		}
		return m;
	}

	public static AMatrix deepCopy(AMatrix m) {
		return create(m);
	}


}
