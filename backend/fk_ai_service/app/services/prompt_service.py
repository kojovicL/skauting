from typing import Any, Dict
from pydantic import BaseModel


class AnalysisContext(BaseModel):
    user_role: str = "User"
    domain: str = "General"
    additional_constraints: list[str] | None = None


class PromptService:
    """Handles prompt template rendering and system instruction construction."""

    SYSTEM_INSTRUCTIONS: Dict[str, str] = {
        "default": (
            "You are an expert AI assistant. Provide concise, accurate, and structured responses. "
            "Follow all formatting constraints strictly."
        ),
        "data_analyst": (
            "You are a Senior Data Scientist and Analyst. Provide insights backed by logic, "
            "highlight key statistical anomalies, and format findings clearly using markdown."
        ),
        "tactical_analyst": (
            "Ti si elitni glavni analitičar u stručnom štabu elitnog fudbalskog kluba. "
            "Tvoj zadatak je da pružiš duboke taktičke uvide ukrštanjem statistike, "
            "učinka pojedinaca i momentuma utakmice. Budi direktan, koristi stručnu fudbalsku "
            "terminologiju i ne koristi uvodne fraze."
        ),
        "seasonal_scout": (
            "Ti si elitni glavni fudbalski skaut. Tvoj zadatak je da na osnovu serije "
            "pojedinačnih izveštaja sa utakmica tokom jedne sezone kreiraš konačni, "
            "sveobuhvatni sezonski izveštaj za igrača. Fokusiraj se isključivo na fudbalske "
            "veštine, mane, taktičko ponašanje i konzistentnost forme na osnovu prosleđenih "
            "beleški. Zadrži profesionalan i analitički ton. Računaj na to da je u pitanju igrač "
            "kojeg si posmatrao kako igra u drugom timu tokom čitave sezone i trebaš na kraju da daš "
            "preporuku o igraču (da li bi bio od koristi u našem timu)."
        )
    }

    @classmethod
    def get_system_instruction(cls, role: str = "default", extra_context: str | None = None) -> str:
        """Retrieves and enhances system instructions for a given role."""
        base_instruction = cls.SYSTEM_INSTRUCTIONS.get(role, cls.SYSTEM_INSTRUCTIONS["default"])
        if extra_context:
            return f"{base_instruction}\n\nAdditional Guidance:\n{extra_context}"
        return base_instruction

    @staticmethod
    def build_analysis_prompt(data: Dict[str, Any], context: AnalysisContext | None = None) -> str:
        """Formats raw input data into a clean structured prompt."""
        prompt_parts = ["Analyze the following data payload and extract key insights:"]
        
        if context:
            prompt_parts.append(f"\nDomain Context: {context.domain}")
            if context.additional_constraints:
                constraints = "\n".join(f"- {c}" for c in context.additional_constraints)
                prompt_parts.append(f"Constraints:\n{constraints}")

        prompt_parts.append("\n--- Data Payload ---")
        for key, value in data.items():
            prompt_parts.append(f"{key}: {value}")
        prompt_parts.append("--- End Payload ---")

        return "\n".join(prompt_parts)

    @staticmethod
    def build_seasonal_summary_prompt(payload: dict) -> str:
        player = payload.get("playerName", "Igrač")
        league = payload.get("leagueName", "Nepoznata Liga")
        year = payload.get("seasonYear", "Nepoznata Godina")
        commentaries = payload.get("matchCommentaries", [])

        prompt = f"Napravi sveobuhvatan izveštaj na kraju sezone za igrača {player} (Takmičenje: {league}, Sezona: {year}).\n\n"
        prompt += "Ovo su beleške skauta sa pojedinačnih utakmica tokom sezone:\n"

        for i, text in enumerate(commentaries):
            prompt += f"- Utakmica {i+1}: {text}\n"

        prompt += "\nZADATAK: Sintetiši ove beleške u jedan koherentan tekstualni izveštaj. Ne pominji brojeve utakmica. "
        prompt += "Fokusiraj se na ukupan utisak, napredak, ponavljajuće obrasce grešaka i preporuku za sledeću sezonu."

        return prompt

    @staticmethod
    def build_tactical_prompt(payload_data: Dict[str, Any], match_title: str) -> str:
        return f"""Uradi duboku dekonstrukciju upravo završene utakmice: {match_title}.

        --- ULAZNI PODACI IZ NAŠIH BAZA (NEO4J, INFLUXDB, DROOLS) ---
        1. STATISTIKA (Očekivano vs Ostvareno):
        Očekivano (Pre-match predikcija):
        {payload_data.get('expected')}
        
        Ostvareno (Realnost):
        {payload_data.get('actual')}
        
        2. UČINAK IGRAČA (Performanse):
        Najbolji na terenu: 
        {payload_data.get('best')}
        
        Pojedinci koji su podbacili: 
        {payload_data.get('weak')}
        
        3. VREMENSKA DINAMIKA MEČA (Momentum po intervalima):
        {payload_data.get('timeline')}
        
        4. DETEKTOVANE TAKTIČKE ANOMALIJE (Sistemski okidači):
        {payload_data.get('anomalies')}
        ------------------------------------
        
        ZADATAK:
        - Napravi korelaciju: Poveži padove u vremenskoj dinamici sa igračima koji su podbacili i taktičkim anomalijama.
        - Objasni ZAŠTO je stvarna statistika odstupila od očekivane na osnovu ovih faktora.
        
        FORMATIRAJ ODGOVOR U MARKDOWNU KORISTEĆI SLEDEĆA 3 NASLOVA:
        ### 1. Sinteza meča (Očekivano vs Realnost)
        ### 2. Dinamika i padovi u igri (Ukrštanje momentuma, anomalija i učinka pojedinaca)
        ### 3. Akcioni plan za stručni štab"""