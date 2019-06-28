package model;

import java.util.ArrayList;
import java.util.List;

public class AvailabilitySearchParams {
    public String StartDate;
    public String Nights;
    public String CategoryId;
    public List<String> UnitTypeIds = new ArrayList<>();
    public boolean ShowOnlyAdaUnits;
    public String ShowOnlyTentSiteUnits;
    public String ShowOnlyRvSiteUnits;
    public String MinimumVehicleLength;
    public String ShowSiteUnitsName;
    public List<String> ParkFinder = new ArrayList<>();
    public String chooseActivity;
    public boolean IsPremium;
    public List<String> UnitTypesCategory = new ArrayList<>();
}