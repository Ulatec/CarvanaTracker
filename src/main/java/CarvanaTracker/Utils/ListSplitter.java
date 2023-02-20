package CarvanaTracker.Utils;

import CarvanaTracker.Model.VINEntry;

import java.util.ArrayList;
import java.util.List;

public class ListSplitter {

    public static List<List<VINEntry>> split(List<VINEntry> fulllist, int numOfOutputs) {
        // get size of the list
        List<VINEntry> list = fulllist.subList(0,fulllist.size());
        int size = list.size();
        ArrayList<List<VINEntry>> lists = new ArrayList<>();
        int lastDivider = 0;
        for(int i = 0;i < numOfOutputs; i++){
            int newDivider = (int) Math.round(size*((i+1)/(double)numOfOutputs));
            lists.add(new ArrayList<>(list.subList(lastDivider, newDivider)));
            lastDivider = newDivider + 1;
        }
        // return an List array to accommodate both lists
        return lists;
    }

}
