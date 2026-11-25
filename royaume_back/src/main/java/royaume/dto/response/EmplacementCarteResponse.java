package royaume.dto.response;

public record EmplacementCarteResponse(
        Integer id,
        int numero,
        int coordX,
        int coordY,
        boolean occupe,
        Integer carteId,
        Integer royaumeId,
        String name
) {

}
