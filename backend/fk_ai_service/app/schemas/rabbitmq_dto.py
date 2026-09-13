from pydantic import BaseModel
from typing import List

class SeasonalSummaryRequest(BaseModel):
    seasonalReportId: int
    playerName: str
    leagueName: str
    seasonYear: int
    matchCommentaries: List[str]

class SeasonalSummaryResponse(BaseModel):
    seasonalReportId: int
    summary: str