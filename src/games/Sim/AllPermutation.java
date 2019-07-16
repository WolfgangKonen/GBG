package games.Sim;

class AllPermutation  
{ 
  
    // The input array for permutation 
    private final int Arr[]; 
  
    // Index array to store indexes of input array 
    private int Indexes[]; 
  
    // The index of the first "increase" 
    // in the Index array which is the smallest 
    // i such that Indexes[i] < Indexes[i + 1] 
    private int Increase; 
  
    // Constructor 
    public AllPermutation(int arr[]) 
    { 
        this.Arr = arr; 
        this.Increase = -1; 
        this.Indexes = new int[this.Arr.length]; 
    } 
  
    // Initialize and output 
    // the first permutation 
    public int[]  GetFirst() 
    { 
  
        // Allocate memory for Indexes array 
        this.Indexes = new int[this.Arr.length]; 
  
        // Initialize the values in Index array 
        // from 0 to n - 1 
        for (int i = 0; i < Indexes.length; ++i)  
        { 
            this.Indexes[i] = i; 
        } 
  
        // Set the Increase to 0 
        // since Indexes[0] = 0 < Indexes[1] = 1 
        this.Increase = 0; 
  
        // Output the first permutation 
        return this.Output(); 
    } 
  
    // Function that returns true if it is 
    // possible to generate the next permutation 
    public boolean HasNext() 
    { 
  
        // When Increase is in the end of the array, 
        // it is not possible to have next one 
        return this.Increase != (this.Indexes.length - 1); 
    } 
  
    // Output the next permutation 
    public int[] GetNext() 
    { 
  
        // Increase is at the very beginning 
        if (this.Increase == 0)  
        { 
  
            // Swap Index[0] and Index[1] 
            this.Swap(this.Increase, this.Increase + 1); 
  
            // Update Increase 
            this.Increase += 1; 
            while (this.Increase < this.Indexes.length - 1
                && this.Indexes[this.Increase] 
                        > this.Indexes[this.Increase + 1])  
            { 
                ++this.Increase; 
            } 
        } 
        else
        { 
  
            // Value at Indexes[Increase + 1] is greater than Indexes[0] 
            // no need for binary search, 
            // just swap Indexes[Increase + 1] and Indexes[0] 
            if (this.Indexes[this.Increase + 1] > this.Indexes[0])  
            { 
                this.Swap(this.Increase + 1, 0); 
            } 
            else
            { 
  
                // Binary search to find the greatest value 
                // which is less than Indexes[Increase + 1] 
                int start = 0; 
                int end = this.Increase; 
                int mid = (start + end) / 2; 
                int tVal = this.Indexes[this.Increase + 1]; 
                while (!(this.Indexes[mid]<tVal&& this.Indexes[mid - 1]> tVal))  
                { 
                    if (this.Indexes[mid] < tVal) 
                    { 
                        end = mid - 1; 
                    } 
                    else 
                    { 
                        start = mid + 1; 
                    } 
                    mid = (start + end) / 2; 
                } 
  
                // Swap 
                this.Swap(this.Increase + 1, mid); 
            } 
  
            // Invert 0 to Increase 
            for (int i = 0; i <= this.Increase / 2; ++i) 
            { 
                this.Swap(i, this.Increase - i); 
            } 
  
            // Reset Increase 
            this.Increase = 0; 
        } 
        return this.Output(); 
    } 
  
    // Function to output the input array 
    private int [] Output() 
    { 
    	int [] temp = new int[Arr.length];
        for (int i = 0; i < this.Indexes.length; ++i)  
        { 
            // Indexes of the input array 
            // are at the Indexes array 
        	temp[i] = this.Arr[this.Indexes[i]];
            //System.out.print(this.Arr[this.Indexes[i]]); 
            //System.out.print(" "); 
        } 
        //System.out.println();
        return temp;
    } 
  
    // Swap two values in the Indexes array 
    private void Swap(int p, int q) 
    { 
        int tmp = this.Indexes[p]; 
        this.Indexes[p] = this.Indexes[q]; 
        this.Indexes[q] = tmp; 
    } 
} 