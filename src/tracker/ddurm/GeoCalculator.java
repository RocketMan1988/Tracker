/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tracker.ddurm;

/**
 *
 * @author Sardon
 */
public class GeoCalculator {
    
    double elevationAngle;
    double satelliteLong;
    double siteLong;
    double siteLat;
    double differenceLog;
    
    double siteX;
    double siteY;
    double siteZ;
    
    double balloonX;
    double balloonY;
    double balloonZ;
    
    static float calculatedDistance = Float.NaN;
    static float calculatedAzimuth = Float.NaN;
    
    public static final int LAT_LON_CONVERT = 1000000;
    public static final double EARTH_CIRCUMFRENCE = 40075;
    public static final double EARTH_RADIUS = 6360;
    public static final double GPS_SAT_ORBIT = 20200;
    public static final double SAT_HEIGHT_FROM_CENTER = EARTH_RADIUS + GPS_SAT_ORBIT;
    
    public void GeoCalculator(){
        
    }
    
    public double calculateAzimuth(double satelliteLong, double siteLong, double siteLat){
        satelliteLong = Math.toRadians(satelliteLong);
        siteLong = Math.toRadians(siteLong);
        siteLat = Math.toRadians(siteLat);
        
        differenceLog = satelliteLong - siteLong;
        return Math.toDegrees(Math.atan((Math.cos(differenceLog)*Math.cos(siteLat)-.1512)/(Math.sqrt((1-Math.pow(Math.cos(differenceLog), 2)*(Math.pow(Math.cos(siteLat), 2)))))));
    }
    
    public double bearing(double lat1, double lon1, double lat2, double lon2){
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff= Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }
    
     public double distFrom(double lat1, double lng1, double lat2, double lng2) {
    double earthRadius = 6371; //kilometers
    double dLat = Math.toRadians(lat2-lat1);
    double dLng = Math.toRadians(lng2-lng1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
               Math.sin(dLng/2) * Math.sin(dLng/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    double dist = (earthRadius * c);

    return dist;
    
     }
    /*
 * Calculate distance between two points in latitude and longitude taking
 * into account height difference. If you are not interested in height
 * difference pass 0.0. Uses Haversine method as its base.
 * 
 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
 * el2 End altitude in meters
 */
public double distance(double lat1, double lat2, double lon1, double lon2,
        double el1, double el2) {

    final int R = 6371; // Radius of the earth

    Double latDistance = deg2rad(lat2 - lat1);
    Double lonDistance = deg2rad(lon2 - lon1);
    Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters

    double height = el1 - el2;
    distance = Math.pow(distance, 2) + Math.pow(height, 2);
    return Math.sqrt(distance);
}

        private void convertToCartisianSite(double R, double latitude, double longitude){
            R = Math.toRadians(R);
            latitude = Math.toRadians(latitude);
            longitude = Math.toRadians(longitude);
            
            siteX = R*Math.cos(latitude)*Math.cos(longitude);
            siteY = R*Math.cos(latitude)*Math.sin(longitude);
            siteZ = R*Math.sin(latitude);
            //siteZ = ((1-Math.pow(Math.E, 2))*R)*Math.sin(latitude);
            
            //System.out.println(siteX);
            //System.out.println(siteY);
            //System.out.println(siteZ);
        }
        
        private void convertToCartisianSatillite(double R, double latitude, double longitude){
            R = Math.toRadians(R);
            latitude = Math.toRadians(latitude);
            longitude = Math.toRadians(longitude);
            
            balloonX = R*Math.cos(latitude)*Math.cos(longitude);
            balloonY = R*Math.cos(latitude)*Math.sin(longitude);
            balloonZ = R*Math.sin(latitude);
            //balloonZ = ((1-Math.pow(Math.E, 2))*R)*Math.sin(latitude);
            
            //System.out.println(balloonX);
            //System.out.println(balloonY);
            //System.out.println(balloonZ);
        }

        public double cartisianDistance(double Rsite, double latitudeSite, double longitudeSite, double Rballoon, double latitudeBalloon, double longitudeBalloon){
            this.convertToCartisianSatillite(Rballoon, latitudeBalloon, longitudeBalloon);
            this.convertToCartisianSite(Rsite, latitudeSite, longitudeSite);
            
            return Math.sqrt(Math.pow(balloonX - siteX, 2) + Math.pow(balloonY - siteY, 2) + Math.pow(balloonZ - siteZ, 2));
        }
        
        public double cartisianDistance2(double Rsite, double latitudeSite, double longitudeSite, double Rballoon, double latitudeBalloon, double longitudeBalloon){
            this.convertCartisianDistanceBalloon(Rballoon, latitudeBalloon, longitudeBalloon);
            this.convertCartisianDistanceSite(Rsite, latitudeSite, longitudeSite);
            
            return Math.sqrt(Math.pow(balloonX - siteX, 2) + Math.pow(balloonY - siteY, 2) + Math.pow(balloonZ - siteZ, 2));
        }
        
        public double calculateElevationAngle(double Rsite, double latitudeSite, double longitudeSite, double Rballoon, double latitudeBalloon, double longitudeBalloon){
            double a = cartisianDistance2(Rsite, latitudeSite, longitudeSite, Rballoon, latitudeBalloon, longitudeBalloon);
            double b = 6378.137 + Rsite;
            double c = 6378.137 + Rballoon;
            
            double alpha = Math.toDegrees(Math.acos((-Math.pow(a, 2) + Math.pow(b, 2) + Math.pow(c, 2))/(2*b*c)));
            double alpha2 = 90 - alpha;
            double theta = 180 - Math.toDegrees(Math.asin(c/a*Math.sin(Math.toRadians(alpha))));
           
            //System.out.println(a);
            //System.out.println(b);
            //System.out.println(c);
            //System.out.println(alpha);
            //System.out.println(alpha2);
            //System.out.println(theta);        
            
            return theta - alpha2;
        }
        
        private void convertCartisianDistanceBalloon(double alt, double latitude, double longitude){
            double esq = 6.69437999014 * 0.001;
            double a = 6378.137;
            double xi;
            //Units are degrees and kilometers.
            
            latitude = Math.toRadians(latitude);
            longitude = Math.toRadians(longitude);

            xi = Math.sqrt(1 - esq * Math.sin(latitude));
            balloonX = (a / xi + alt)*Math.cos(latitude)*Math.cos(longitude);
            balloonY = (a / xi + alt)*Math.cos(latitude)*Math.sin(longitude);
            balloonZ = (a / xi * (1 - esq) + alt)*Math.sin(latitude);
            //balloonZ = ((1-Math.pow(Math.E, 2))*R)*Math.sin(latitude);
            
            //System.out.println(balloonX);
            //System.out.println(balloonY);
            //System.out.println(balloonZ);
        }
        
          private void convertCartisianDistanceSite(double alt, double latitude, double longitude){
            double esq = 6.69437999014 * 0.001;
            double a = 6378.137;
            double xi;
            //Units are degrees and kilometers.
            
            latitude = Math.toRadians(latitude);
            longitude = Math.toRadians(longitude);

            xi = Math.sqrt(1 - esq * Math.sin(latitude));
            siteX = (a / xi + alt)*Math.cos(latitude)*Math.cos(longitude);
            siteY = (a / xi + alt)*Math.cos(latitude)*Math.sin(longitude);
            siteZ = (a / xi * (1 - esq) + alt)*Math.sin(latitude);
            //balloonZ = ((1-Math.pow(Math.E, 2))*R)*Math.sin(latitude);
            
            //System.out.println(siteX);
            //System.out.println(siteY);
            //System.out.println(siteZ);
        }
          
          
        
	private static void calculateDistanceAndAzimuth(double d, double d1, double d2, double d3){
        // TODO: This code is huge. Can it be minimized?
		double d4 = Math.toRadians(d);
        double d5 = Math.toRadians(d1);
		double d6 = Math.toRadians(d2);
		double d7 = Math.toRadians(d3);
		double d8 = 0.0033528106647474805D;
		// TODO: Why are these given 0 values?
        double d9 = 0.0D;
        double d10 = 0.0D;
        double d20 = 0.0D;
        double d22 = 0.0D;
        double d24 = 0.0D;
        double d25 = 0.0D;
        double d26 = 0.0D;
        double d28 = 0.0D;
        double d29 = 0.0D;
        double d30 = 0.0D;
        double d31 = 0.0D;
        double d32 = 0.0D;
        double d33 = 5.0000000000000003E-10D;
        int i = 1;
        byte byte0 = 100;
        if(d4 == d6 && (d5 == d7 || Math.abs(Math.abs(d5 - d7) - 6.2831853071795862D) < d33))
        {
            calculatedDistance = 0.0F;
            calculatedAzimuth = 0.0F;
            return;
        }
        
        // TODO: Use our version of Math.PI throughout, including 2pi.
        if(d4 + d6 == 0.0D && Math.abs(d5 - d7) == 3.1415926535897931D)
            d4 += 1.0000000000000001E-05D;
        double d11 = 1.0D - d8;
        double d12 = d11 * Math.tan(d4);
        double d13 = d11 * Math.tan(d6);
        double d14 = 1.0D / Math.sqrt(1.0D + d12 * d12);
        double d15 = d14 * d12;
        double d16 = 1.0D / Math.sqrt(1.0D + d13 * d13);
        double d17 = d14 * d16;
        double d18 = d17 * d13;
        double d19 = d18 * d12;
        d9 = d7 - d5;
        
        for(d32 = d9 + 1.0D; i < byte0 && Math.abs(d32 - d9) > d33; d9 = ((1.0D - d31) * d9 * d8 + d7) - d5)
        {
            i++;
            double d21 = Math.sin(d9);
            double d23 = Math.cos(d9);
            d12 = d16 * d21;
            d13 = d18 - d15 * d16 * d23;
            d24 = Math.sqrt(d12 * d12 + d13 * d13);
            d25 = d17 * d23 + d19;
            d10 = Math.atan2(d24, d25);
            double d27 = (d17 * d21) / d24;
            d28 = 1.0D - d27 * d27;
            d29 = 2D * d19;
            if(d28 > 0.0D)
                d29 = d25 - d29 / d28;
            d30 = -1D + 2D * d29 * d29;
            d31 = (((-3D * d28 + 4D) * d8 + 4D) * d28 * d8) / 16D;
            d32 = d9;
            d9 = ((d30 * d25 * d31 + d29) * d24 * d31 + d10) * d27;
        }
        
        double d34 = mod(Math.atan2(d12, d13), 6.2831853071795862D);
        d9 = Math.sqrt((1.0D / (d11 * d11) - 1.0D) * d28 + 1.0D);
        d9++;
        d9 = (d9 - 2D) / d9;
        d31 = ((d9 * d9) / 4D + 1.0D) / (1.0D - d9);
        d32 = (d9 * d9 * 0.375D - 1.0D) * d9;
        d9 = d30 * d25;
        
        double d35 = ((((((d24 * d24 * 4D - 3D) * (1.0D - d30 - d30) * d29 * d32) / 6D - d9) * d32) / 4D + d29) * d24 * d32 + d10) * d31 * 6378137D * d11;
        if((double)Math.abs(i - byte0) < d33)
        {
            calculatedDistance = (0.0F / 0.0F);
            calculatedAzimuth = (0.0F / 0.0F);
            return;
        }
        d34 = (180D * d34) / 3.1415926535897931D;
        calculatedDistance = (float)d35;
        calculatedAzimuth = (float)d34;
        if(d == 90D)
            calculatedAzimuth = 180F;
        else
        if(d == -90D)
            calculatedAzimuth = 0.0F;
    }
        
    private static double mod(double d, double d1){
        return d - d1 * Math.floor(d / d1);
    }

private double deg2rad(double deg) {
    return (deg * Math.PI / 180.0);
}
    
    public double calculateElevation(double satelliteLong, double siteLong, double siteLat){
        satelliteLong = Math.toRadians(satelliteLong);
        siteLong = Math.toRadians(siteLong);
        siteLat = Math.toRadians(siteLat);
        
        differenceLog = satelliteLong - siteLong;
        return Math.toDegrees(Math.toRadians(180) + Math.atan(Math.tan(differenceLog)/Math.sin(siteLat)));
    }
    
    
}
