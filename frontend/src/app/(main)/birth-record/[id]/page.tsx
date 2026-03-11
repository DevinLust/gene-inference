import { fetchBirthRecordById } from '@/app/lib/data';
import PhenotypesAtBirthTable from '@/app/ui/relationship/phenotypes-at-birth-table';
import { RelationshipDetails, SheepDetails, BreedSheepButton, RecordChildButton } from '@/app/ui/buttons';
import BirthRecordDeleteButton from '@/app/ui/relationship/birth-record-delete-button';
import { notFound } from 'next/navigation';

export default async function BirthRecordPage(props: { params: Promise<{ id: string }> }) {
    const params = await props.params;
    const id = params.id;

    const birthRecord = await fetchBirthRecordById(id);
    const parent1 = birthRecord.parentRelationshipSummary.parent1;
    const parent2 = birthRecord.parentRelationshipSummary.parent2;

    if (!birthRecord) {
        notFound();
    }

    return (
        <div>
            <h1 className="text-2xl">Birth Record</h1>

            {/* Id */}
            <div className="flex flex-wrap justify-between mt-4">
                <p>ID: {birthRecord.id}</p>
                <div className="flex flex-wrap justify-start gap-2">
                    <BreedSheepButton parent1Id={parent1.id} parent2Id={parent2.id} label="Breed More +"/>
                    <RecordChildButton parent1Id={parent1.id} parent2Id={parent2.id} label="Record Another +" />
                    <BirthRecordDeleteButton brId={birthRecord.id} />
                </div>
            </div>

            {/* Relationship ID */}
            <div className="mt-4">
                <p>
                    Relationship
                    <RelationshipDetails relId={birthRecord.parentRelationshipSummary.id} />
                </p>
                <p>
                    Parent 1:
                    <SheepDetails sheepId={parent1.id}>
                        <span>{parent1.name ?? "(unnamed)"}</span>
                    </SheepDetails>
                </p>
                <p>
                    Parent 2:
                    <SheepDetails sheepId={parent2.id}>
                        <span>{parent2.name ?? "(unnamed)"}</span>
                    </SheepDetails>
                </p>
            </div>

            {/* Child */}
            <p className="mt-4">
                Child:
                {birthRecord.child ?
                    <SheepDetails sheepId={birthRecord.child.id} className="mt-2">
                        <span>{birthRecord.child.name ?? "(unnamed)"}</span>
                    </SheepDetails>
                    :
                    <span> not saved</span>
                }
            </p>


            {/* Phenotypes at birth */}
            <PhenotypesAtBirthTable birthRecord={birthRecord}/>
        </div>
    );
}
