package CarvanaTracker.Service;


import CarvanaTracker.Model.VINEntry;
import CarvanaTracker.Repository.VinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class VinService {

    @Autowired
    private VinRepository vinRepository;

    public List<VINEntry> findAll(){
        return vinRepository.findAll();
    }
    public List<VINEntry> findAllVinsNeedToBeSearched(){
        List<VINEntry> allVins = vinRepository.findAllBySold(false);
        return allVins;
    }

    public Optional<VINEntry> findVin(String vin){
        return vinRepository.findByVin(vin);
    }

    public void save(VINEntry vinEntry){
        vinRepository.save(vinEntry);
    }

    public void batchSave(List<VINEntry> vinEntryList){
        //Build unique list of entries. Do not save multiple instances of same VIN. Will cause locking exception.
            List<VINEntry> uniqueList = new ArrayList<>();
        HashMap<String, VINEntry> uniqueEntries = new HashMap<>();
            for(VINEntry vinEntry : vinEntryList){
                uniqueEntries.putIfAbsent(vinEntry.getVin(), vinEntry);
            }
        for(Map.Entry<String, VINEntry> entry: uniqueEntries.entrySet()) {
            uniqueList.add(entry.getValue());
        }

        vinRepository.saveAll(uniqueList);
    }


    public List<VINEntry> findAllWithVin(String vin) {
        return vinRepository.findAllByVin(vin);
    }
}
